package gov.ismonnet.cardhelp.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.activity.DetectionFragment.OnListFragmentInteractionListener;
import gov.ismonnet.cardhelp.core.Detection;

public class DetectionRecyclerViewAdapter extends RecyclerView.Adapter<DetectionRecyclerViewAdapter.ViewHolder> {

    private final IntFunction<Detection> items;
    private final IntSupplier itemsCount;

    private final OnListFragmentInteractionListener listener;

    DetectionRecyclerViewAdapter(IntFunction<Detection> items,
                                 IntSupplier itemsCount,
                                 OnListFragmentInteractionListener listener) {
        this.items = items;
        this.itemsCount = itemsCount;
        this.listener = listener;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_detection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // TODO: fix this thing duplicating stuff, not ordering, etc

        final Detection detection = items.apply(position);
        holder.item = detection;
        holder.thumbnailView.setImageBitmap(detection.getThumbnail());
        holder.timestampView.setText(detection.getTimestamp().toString());
        holder.gameView.setText(detection.getGame());
        holder.scoreView.setText(detection.getScore());

        holder.view.setOnClickListener(v -> {
            if (listener == null)
                return;
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            listener.onListFragmentInteraction(holder.item);
        });
    }

    @Override
    public int getItemCount() {
        return itemsCount.getAsInt();
    }

    // Fix dupes:
    // https://stackoverflow.com/a/43730205

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // end

    public class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView thumbnailView;
        final TextView timestampView;
        final TextView gameView;
        final TextView scoreView;

        Detection item;

        ViewHolder(View view) {
            super(view);

            this.view = view;
            this.thumbnailView = view.findViewById(R.id.thumbnail_view);
            this.timestampView = view.findViewById(R.id.timestamp);
            this.gameView = view.findViewById(R.id.game_content);
            this.scoreView = view.findViewById(R.id.score_content);
        }

        @Override
        public String toString() {
            return super.toString() +
                    " '" + timestampView.getText() + "'" +
                    " '" + gameView.getText() + "'" +
                    " '" + scoreView.getText() + "'";
        }
    }
}
