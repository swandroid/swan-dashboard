package acba.acbaapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import swan.dashboard.R;

/**
 * Created by Alex on 24-May-16.
 */
public class GridViewItemAdapter extends BaseAdapter {

    private Context context;
    private InformationCardsData data;

    public GridViewItemAdapter(Context context, InformationCardsData data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.getTile(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.card_view, null);
            holder = new ViewHolder(convertView);

            int screenHeight = ((Activity) context).getWindowManager()
                    .getDefaultDisplay().getHeight();

            convertView.setMinimumHeight(screenHeight / 2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        InformationCard tile = data.getTile(position);
        holder.description.setText(tile.getDescription());
        holder.value.setText(tile.getValue());
        holder.image.setImageResource(tile.getImageResourceId());

        return convertView;
    }

    static class ViewHolder {
        TextView description, value;
        ImageView image;

        ViewHolder(View baseView) {
            description = (TextView)baseView.findViewById(R.id.itemDescriptionTextView);
            value = (TextView)baseView.findViewById(R.id.itemValueTextView);
            image = (ImageView)baseView.findViewById(R.id.imageView);
        }
    }
}
