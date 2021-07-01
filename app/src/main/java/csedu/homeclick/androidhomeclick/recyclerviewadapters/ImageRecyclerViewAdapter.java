package csedu.homeclick.androidhomeclick.recyclerviewadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import csedu.homeclick.androidhomeclick.R;

public class ImageRecyclerViewAdapter extends  RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder>{
    private Context context;
    private static final String TAG = "ImagesRecVA";
    List<?> imageURL = new ArrayList<>();
    private OnPhotoClickListener onPhotoClickListener;

    public ImageRecyclerViewAdapter() {

    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ImageRecyclerViewAdapter(Context context, List<?> imageURL) {
        this.context = context;
        this.imageURL = imageURL;
    }

    public ImageRecyclerViewAdapter(Context context, List<?> imageURL, OnPhotoClickListener onPhotoClickListener) {
        this.context = context;
        this.imageURL = imageURL;
        this.onPhotoClickListener = onPhotoClickListener;
    }

    public void setUrlArrayList(List<?> imageURL) {
        this.imageURL = imageURL;
        notifyDataSetChanged();
    }

    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }



    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_space, parent, false);
        ImageRecyclerViewAdapter.ViewHolder holder = new ImageRecyclerViewAdapter.ViewHolder(view, onPhotoClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        if(imageURL.get(position).getClass().equals(String.class)) {
            Log.i(TAG, "in on bind view holder, glide not working?");
            Log.i(TAG, imageURL.get(position).toString());
            RequestOptions options = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.loading_photo)
                    .error(R.drawable.icon_error);
            Glide.with(context).load(imageURL.get(position)).apply(options).into(holder.imageView);
        } else {
            Glide.with(context).load(imageURL.get(position)).into(holder.imageView);
        }

    }

    @Override
    public int getItemCount() {
        return imageURL.size();
    }


    /*
    * Holder inner class starts from here
    *
    * */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        private final ImageView imageView;
        private final OnPhotoClickListener onPhotoClickListener;

        public ViewHolder(@NonNull @NotNull View itemView, OnPhotoClickListener onPhotoClickListener) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.ad_photos);
            this.onPhotoClickListener = onPhotoClickListener;
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            onPhotoClickListener.onPhotoClick(getAdapterPosition());
            return false;
        }
    }

    public interface OnPhotoClickListener{
        void onPhotoClick(int position);
    }
}
