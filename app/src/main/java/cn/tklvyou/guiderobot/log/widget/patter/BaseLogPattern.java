package cn.tklvyou.guiderobot.log.widget.patter;





import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot.log.widget.utils.Utils;

/**
 * @author :zhoujian
 * @description : BaseLogPattern
 * @company :途酷科技
 * @date 2018年09月07日上午 10:01
 * @Email: 971613168@qq.com
 */
public abstract class BaseLogPattern {

    public static class PlainBaseLogPattern extends BaseLogPattern {

        private final String string;

        public PlainBaseLogPattern(int count, int length, String string) {
            super(count, length);
            this.string = string;
        }

        @Override
        protected String doApply(StackTraceElement caller) {
            return string;
        }
    }

    public static class DateBaseLogPattern extends BaseLogPattern {

        private final SimpleDateFormat dateFormat;

        public DateBaseLogPattern(int count, int length, String dateFormat) {
            super(count, length);
            if (dateFormat != null) {
                this.dateFormat = new SimpleDateFormat(dateFormat);
            } else {
                this.dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            }

        }

        @Override
        protected String doApply(StackTraceElement caller) {
            return dateFormat.format(new Date());
        }

    }

    public static class CallerBaseLogPattern extends BaseLogPattern {

        private int callerCount;
        private int callerLength;

        public CallerBaseLogPattern(int count, int length, int callerCount, int callerLength) {
            super(count, length);
            this.callerCount = callerCount;
            this.callerLength = callerLength;
        }

        @Override
        protected String doApply(StackTraceElement caller) {
            if (caller == null) {
                throw new IllegalArgumentException("Caller not found");
            } else {
                String callerString;
                if (caller.getLineNumber() < 0) {
                    callerString = String.format("%s#%s", caller.getClassName(), caller.getMethodName());
                } else {
                    String stackTrace = caller.toString();
                    stackTrace = stackTrace.substring(stackTrace.lastIndexOf('('), stackTrace.length());
                    callerString = String.format("%s.%s%s", caller.getClassName(), caller.getMethodName(), stackTrace);
                }
                try {
                    return Utils.shortenClassName(callerString, callerCount, callerLength);
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        }

        @Override
        protected boolean isCallerNeeded() {
            return true;
        }
    }

    public static class ConcatenateBaseLogPattern extends BaseLogPattern {

        private final List<BaseLogPattern> patternList;

        public ConcatenateBaseLogPattern(int count, int length, List<BaseLogPattern> patternList) {
            super(count, length);
            this.patternList = new ArrayList<>(patternList);
        }

        public void addPattern(BaseLogPattern pattern) {
            patternList.add(pattern);
        }

        @Override
        protected String doApply(StackTraceElement caller) {
            StringBuilder builder = new StringBuilder();
            for (BaseLogPattern pattern : patternList) {
                builder.append(pattern.apply(caller));
            }
            return builder.toString();
        }

        @Override
        protected boolean isCallerNeeded() {
            for (BaseLogPattern pattern : patternList) {
                if (pattern.isCallerNeeded()) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class ThreadNameBaseLogPattern extends BaseLogPattern {
        public ThreadNameBaseLogPattern(int count, int length) {
            super(count, length);
        }

        @Override
        protected String doApply(StackTraceElement caller) {
            return Thread.currentThread().getName();
        }
    }

    private final int count;
    private final int length;

    private BaseLogPattern(int count, int length) {
        this.count = count;
        this.length = length;
    }

    public final String apply(StackTraceElement caller) {
        String string = doApply(caller);
        return Utils.shorten(string, count, length);
    }

    /**
     * doApply
     *
     * @param caller
     * @return
     */
    protected abstract String doApply(StackTraceElement caller);

    protected boolean isCallerNeeded() {
        return false;
    }

    public static BaseLogPattern compile(String pattern) {
        try {
            return pattern == null ? null : new Compiler().compile(pattern);
        } catch (Exception e) {
            return new PlainBaseLogPattern(0, 0, pattern);
        }
    }

    public static class Compiler {

        private String patternString;
        private int position;
        private List<ConcatenateBaseLogPattern> queue;

        public static final Pattern PERCENT_PATTERN = Pattern.compile("%%");
        public static final Pattern NEWLINE_PATTERN = Pattern.compile("%n");
        public static final Pattern CALLER_PATTERN = Pattern.compile("%([+-]?\\d+)?(\\.([+-]?\\d+))?caller(\\{([+-]?\\d+)?(\\.([+-]?\\d+))?\\})?");
        public static final Pattern DATE_PATTERN = Pattern.compile("%date(\\{(.*?)\\})?");
        public static final Pattern CONCATENATE_PATTERN = Pattern.compile("%([+-]?\\d+)?(\\.([+-]?\\d+))?\\(");
        public static final Pattern DATE_PATTERN_SHORT = Pattern.compile("%d(\\{(.*?)\\})?");
        public static final Pattern CALLER_PATTERN_SHORT = Pattern.compile("%([+-]?\\d+)?(\\.([+-]?\\d+))?c(\\{([+-]?\\d+)?(\\.([+-]?\\d+))?\\})?");
        public static final Pattern THREAD_NAME_PATTERN = Pattern.compile("%([+-]?\\d+)?(\\.([+-]?\\d+))?thread");
        public static final Pattern THREAD_NAME_PATTERN_SHORT = Pattern.compile("%([+-]?\\d+)?(\\.([+-]?\\d+))?t");

        public BaseLogPattern compile(String string) {
            if (string == null) {
                return null;
            }
            this.position = 0;
            this.patternString = string;
            this.queue = new ArrayList<>();
            queue.add(new ConcatenateBaseLogPattern(0, 0, new ArrayList<BaseLogPattern>()));
            while (string.length() > position) {
                int index = string.indexOf("%", position);
                int bracketIndex = string.indexOf(")", position);
                if (queue.size() > 1 && bracketIndex < index) {
                    queue.get(queue.size() - 1).addPattern(new PlainBaseLogPattern(0, 0, string.substring(position, bracketIndex)));
                    queue.get(queue.size() - 2).addPattern(queue.remove(queue.size() - 1));
                    position = bracketIndex + 1;
                }
                if (index == -1) {
                    queue.get(queue.size() - 1).addPattern(new PlainBaseLogPattern(0, 0, string.substring(position)));
                    break;
                } else {
                    queue.get(queue.size() - 1).addPattern(new PlainBaseLogPattern(0, 0, string.substring(position, index)));
                    position = index;
                    parse();
                }
            }
            return queue.get(0);
        }

        private void parse() {
            Matcher matcher;
            if ((matcher = findPattern(PERCENT_PATTERN)) != null) {
                queue.get(queue.size() - 1).addPattern(new PlainBaseLogPattern(0, 0, "%"));
                position = matcher.end();
                return;
            }
            if ((matcher = findPattern(NEWLINE_PATTERN)) != null) {
                queue.get(queue.size() - 1).addPattern(new PlainBaseLogPattern(0, 0, "\n"));
                position = matcher.end();
                return;
            }
            // the order is important because short logger pattern may match long caller occurrence
            if ((matcher = findPattern(CALLER_PATTERN)) != null || (matcher = findPattern(CALLER_PATTERN_SHORT)) != null) {
                int count = Integer.parseInt(matcher.group(1) == null ? "0" : matcher.group(1));
                int length = Integer.parseInt(matcher.group(3) == null ? "0" : matcher.group(3));
                int countCaller = Integer.parseInt(matcher.group(5) == null ? "0" : matcher.group(5));
                int lengthCaller = Integer.parseInt(matcher.group(7) == null ? "0" : matcher.group(7));
                queue.get(queue.size() - 1).addPattern(new CallerBaseLogPattern(count, length, countCaller, lengthCaller));
                position = matcher.end();
                return;
            }
            if ((matcher = findPattern(DATE_PATTERN)) != null || (matcher = findPattern(DATE_PATTERN_SHORT)) != null) {
                String dateFormat = matcher.group(2);
                queue.get(queue.size() - 1).addPattern(new DateBaseLogPattern(0, 0, dateFormat));
                position = matcher.end();
                return;
            }
            if ((matcher = findPattern(THREAD_NAME_PATTERN)) != null || (matcher = findPattern(THREAD_NAME_PATTERN_SHORT)) != null) {
                int count = Integer.parseInt(matcher.group(1) == null ? "0" : matcher.group(1));
                int length = Integer.parseInt(matcher.group(3) == null ? "0" : matcher.group(3));
                queue.get(queue.size() - 1).addPattern(new ThreadNameBaseLogPattern(count, length));
                position = matcher.end();
                return;
            }
            if ((matcher = findPattern(CONCATENATE_PATTERN)) != null) {
                int count = Integer.parseInt(matcher.group(1) == null ? "0" : matcher.group(1));
                int length = Integer.parseInt(matcher.group(3) == null ? "0" : matcher.group(3));
                queue.add(new ConcatenateBaseLogPattern(count, length, new ArrayList<BaseLogPattern>()));
                position = matcher.end();
                return;
            }

            throw new IllegalArgumentException();
        }

        private Matcher findPattern(Pattern pattern) {
            Matcher matcher = pattern.matcher(patternString);
            return matcher.find(position) && matcher.start() == position ? matcher : null;
        }

    }

    /**
     * log文件正则匹配
     */
    public static class LogFileNamePattern {

        private String patternString;
        private Date date;

        public LogFileNamePattern(String patternString) {
            this.patternString = patternString;
            date = new Date();
        }

        public String doApply() {
            if (patternString == null) {
                return null;
            }
            String temp = patternString;
            Matcher matcher = Compiler.DATE_PATTERN_SHORT.matcher(patternString);
            while (matcher.find()) {
                String format = matcher.group(2);
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
                String dateString = dateFormat.format(date);
                temp = temp.replace(matcher.group(0), dateString);
            }
            TourCooLogUtil.w("奇葩原因:"+temp+"------");
            return temp;
        }
    }

}
