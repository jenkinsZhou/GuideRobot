package cn.tklvyou.guiderobot.adapter;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;


import cn.tklvyou.guiderobot.model.LogInfo;
import cn.tklvyou.guiderobot_new.R;

import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_DEBUG;
import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_ERROR;
import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_INFO;
import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_WTF;

/**
 * @author :JenkinsZhou
 * @description :日志适配器
 * @company :翼迈科技股份有限公司
 * @date 2019年09月05日22:21
 * @Email: 971613168@qq.com
 */
public class LogAdapter extends BaseQuickAdapter<LogInfo, BaseViewHolder> {

    public LogAdapter( ) {
        super(R.layout.item_log_info_layout);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, LogInfo item) {
        if(item == null){
            return;
        }
        TextView textView = helper.getView(R.id.tvLogInfo);
        helper.setText(R.id.tvLogInfo,"日志:"+item.getLogContent());
        switch (item.getLogLevel()) {
            case TYPE_DEBUG:
                textView.setTextColor(ContextCompat.getColor(mContext,R.color.blue33bafc));
                break;
            case TYPE_INFO:
                textView.setTextColor(ContextCompat.getColor(mContext,R.color.green0DBD00));
                break;
            case TYPE_ERROR:
                textView.setTextColor(ContextCompat.getColor(mContext,R.color.redF35757));
                break;
            case TYPE_WTF:
                textView.setTextColor(ContextCompat.getColor(mContext,R.color.yellowFFB400));
                break;
                default:
                    textView.setTextColor(ContextCompat.getColor(mContext,R.color.gray666666));
                    break;
        }
    }
}
