package swan.dashboard.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import swan.dashboard.sensors.InformationCard;
import swan.dashboard.activities.DashboardActivity;
import swan.dashboard.R;

public class SensorsAdapter extends RecyclerView.Adapter<SensorsAdapter.MyViewHolder> {

    private Context context;
    private List<InformationCard> data;
    private int viewType;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView description, value;
        ImageView image;

        public MyViewHolder(View view) {
            super(view);
            description = (TextView)view.findViewById(R.id.itemDescriptionTextView);
            value = (TextView)view.findViewById(R.id.itemValueTextView);
            image = (ImageView)view.findViewById(R.id.imageView);
        }
    }


    public SensorsAdapter(Context context, List<InformationCard> data, int viewType) {
        this.context = context;
        this.data = data;
        this.viewType = viewType;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (this.viewType) {
            case DashboardActivity.VIEW_TYPE_CARD:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
                return new MyViewHolder(itemView);
            case DashboardActivity.VIEW_TYPE_CARD_GROUP:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_header, parent, false);
                return new MyViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final InformationCard tile = data.get(position);
        holder.description.setText(tile.getDescription());
        holder.value.setText(tile.getValue());
        holder.image.setImageResource(tile.getImageResourceId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tile.executeOnClickHandler();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
