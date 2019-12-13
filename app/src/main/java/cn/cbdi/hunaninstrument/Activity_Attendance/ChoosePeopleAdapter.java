package cn.cbdi.hunaninstrument.Activity_Attendance;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.cbdi.hunaninstrument.Bean.AttendanceScene;
import cn.cbdi.hunaninstrument.R;


public class ChoosePeopleAdapter extends RecyclerView.Adapter<ChoosePeopleAdapter.CPViewHolder> {

    SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMddHHmmss");

    SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Context mContext;
    List<AttendanceScene> list;

    public ChoosePeopleAdapter(Context context, List<AttendanceScene> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public ChoosePeopleAdapter.CPViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        ChoosePeopleAdapter.CPViewHolder holder = new ChoosePeopleAdapter.CPViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.layout_cpitemunit, parent, false));
        return holder;
    }


    @Override
    public void onBindViewHolder(ChoosePeopleAdapter.CPViewHolder holder, final int position)
    {
        holder.tv_serial_num.setText(String.valueOf(position+1));
        holder.tv_name.setText(list.get(position).getName());
        try {
            holder.tv_time.setText(formatter2.format(formatter1.parse(list.get(position).getAttendanceTime())));
        }catch (ParseException e){
            Log.e("ParseException",e.toString());
        }
        if(list.get(position).getFaceRecognition()!=0){
            if(list.get(position).getFaceRecognition() == 1){
                holder.tv_workType.setText("上班打卡");
            }else if(list.get(position).getFaceRecognition() == 2){
                holder.tv_workType.setText("下班打卡");
            }
        }else {
            holder.tv_workType.setText(null);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null){
                    onItemClickListener.onClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    ChoosePeopleAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(ChoosePeopleAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }




    class CPViewHolder extends RecyclerView.ViewHolder
    {
        TextView tv_serial_num;
        TextView tv_name;
        TextView tv_time;
        TextView tv_workType;

        public CPViewHolder(View view)
        {
            super(view);
            tv_serial_num = (TextView) view.findViewById(R.id.tv_serial_num) ;
            tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_time = (TextView) view.findViewById(R.id.tv_time);
            tv_workType = (TextView) view.findViewById(R.id.tv_workType);
        }
    }
}
