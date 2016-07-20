package milestone.ewalk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.Map;

import milestone.ewalk.R;
import milestone.ewalk.bean.RankBean;
import milestone.ewalk.widget.CircularImage;

public class RankAdapter extends BaseAdapter {

	private Context context;
	private List<RankBean> rankBeans;
	private LayoutInflater inflater;
    private int type = 1;
    private int myRank = 0;

	public RankAdapter(Context context, List<RankBean> rankBeans,int type) {
		super();
		this.context = context;
		this.rankBeans = rankBeans;
        this.type = type;
	}

    public void setList(List<RankBean> rankBeans) {
        this.rankBeans = rankBeans;
    }

    public void setType(int type) {
        this.type = type;
    }


    public void setMyRank(int myRank) {
        this.myRank = myRank;
    }

    @Override
	public int getCount() {

		return rankBeans.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.item_rank, null);
			viewHolder = new ViewHolder();

			viewHolder.iv_rank =  (ImageView) convertView.findViewById(R.id.iv_rank);
            viewHolder.tv_rank =  (TextView)convertView.findViewById(R.id.tv_rank);
            viewHolder.iv_poster =  (CircularImage)convertView.findViewById(R.id.iv_poster);
            viewHolder.tv_name =  (TextView)convertView.findViewById(R.id.tv_name);
            viewHolder.tv_my_team = (TextView) convertView.findViewById(R.id.tv_my_team);
            viewHolder.tv_core = (TextView) convertView.findViewById(R.id.tv_core);
            viewHolder.tv_company_name = (TextView) convertView.findViewById(R.id.tv_company_name);
            viewHolder.tv_wanbu = (TextView) convertView.findViewById(R.id.tv_wanbu);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

        RankBean rankBean = rankBeans.get(position);
        int rank = rankBean.getRank();

        if(rank==1){
            viewHolder.iv_rank.setImageResource(R.drawable.icon_rank_first);
            viewHolder.iv_rank.setVisibility(View.VISIBLE);
            viewHolder.tv_rank.setVisibility(View.GONE);
            viewHolder.tv_core.setTextColor(Color.parseColor("#FD8C2A"));
        }else if(rank==2){
            viewHolder.iv_rank.setImageResource(R.drawable.icon_rank_second);
            viewHolder.iv_rank.setVisibility(View.VISIBLE);
            viewHolder.tv_rank.setVisibility(View.GONE);
            viewHolder.tv_core.setTextColor(Color.parseColor("#FD8C2A"));
        }else if(rank==3){
            viewHolder.iv_rank.setImageResource(R.drawable.icon_rank_third);
            viewHolder.iv_rank.setVisibility(View.VISIBLE);
            viewHolder.tv_rank.setVisibility(View.GONE);
            viewHolder.tv_core.setTextColor(Color.parseColor("#FD8C2A"));
        }else{
            viewHolder.iv_rank.setVisibility(View.GONE);
            viewHolder.tv_rank.setVisibility(View.VISIBLE);
            viewHolder.tv_rank.setText((position+1)+"");
            viewHolder.tv_core.setTextColor(Color.parseColor("#AAB2B8"));
        }

        if(type == 2 && myRank == rank){
            viewHolder.tv_my_team.setVisibility(View.VISIBLE);
        }else{
            viewHolder.tv_my_team.setVisibility(View.GONE);
        }

        if(type==1){
            viewHolder.tv_company_name.setVisibility(View.VISIBLE);

        }else{
            viewHolder.tv_company_name.setVisibility(View.GONE);
        }
        ImageLoader.getInstance().displayImage(rankBean.getPoster(),viewHolder.iv_poster);
        viewHolder.tv_name.setText(rankBean.getName());
        viewHolder.tv_company_name.setText(rankBean.getCompanyName());
        viewHolder.tv_core.setText(rankBean.getSteps()+"");
        viewHolder.tv_wanbu.setText("万步率:"+rankBean.getWanbu()*100 +"%");
        return convertView;
	}


	static class ViewHolder {
        private ImageView iv_rank;
        private TextView tv_rank;
        private CircularImage iv_poster;
        private TextView tv_name;
        private TextView tv_company_name;
        private TextView tv_my_team;
        private TextView tv_core;
        private TextView tv_wanbu;
	}
}
