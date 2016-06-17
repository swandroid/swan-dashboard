package swan.dashboard;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import acba.acbaapp.InformationCard;
import acba.acbaapp.InformationCardsData;

public class SensorsAdapter extends RecyclerView.Adapter<SensorsAdapter.MyViewHolder> {

    private Context context;
    private InformationCardsData data;

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


    public SensorsAdapter(Context context, InformationCardsData data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case InformationCardsData.TILE_TYPE_NORMAL:
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view, parent, false);
                return new MyViewHolder(itemView);

            case InformationCardsData.TILE_TYPE_GROUP:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_header, parent, false);
                return new MyViewHolder(itemView);

            default:
                Log.e(getClass().getSimpleName(), "Error: unknown tile type");
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InformationCard tile = data.getTile(position);
        holder.description.setText(tile.getDescription());
        holder.value.setText(tile.getValue());
        holder.image.setImageResource(tile.getImageResourceId());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
