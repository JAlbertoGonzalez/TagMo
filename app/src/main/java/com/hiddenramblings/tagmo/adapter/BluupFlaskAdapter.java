package com.hiddenramblings.tagmo.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hiddenramblings.tagmo.GlideApp;
import com.hiddenramblings.tagmo.R;
import com.hiddenramblings.tagmo.TagMo;
import com.hiddenramblings.tagmo.amiibo.Amiibo;
import com.hiddenramblings.tagmo.nfctech.TagUtils;
import com.hiddenramblings.tagmo.settings.BrowserSettings;
import com.hiddenramblings.tagmo.settings.BrowserSettings.BrowserSettingsListener;
import com.hiddenramblings.tagmo.settings.BrowserSettings.VIEW;

import java.util.ArrayList;

public class BluupFlaskAdapter
        extends RecyclerView.Adapter<BluupFlaskAdapter.FlaskViewHolder>
        implements BrowserSettingsListener {

    private final BrowserSettings settings;
    private final OnAmiiboClickListener listener;
    private ArrayList<Amiibo> flaskAmiibo = new ArrayList<>();

    public BluupFlaskAdapter(BrowserSettings settings, OnAmiiboClickListener listener) {
        this.settings = settings;
        this.listener = listener;
    }

    public void setFlaskAmiibo(ArrayList<Amiibo> amiibo) {
        this.flaskAmiibo = amiibo;
    }

    @Override
    public void onBrowserSettingsChanged(BrowserSettings newBrowserSettings,
                                         BrowserSettings oldBrowserSettings) { }

    @Override
    public int getItemCount() {
        return null != flaskAmiibo ? flaskAmiibo.size() : 0;
    }

    @Override
    public long getItemId(int i) {
        return Long.parseLong(flaskAmiibo.get(i).getFlaskTail());
    }

    public Amiibo getItem(int i) {
        return flaskAmiibo.get(i);
    }

    @Override
    public int getItemViewType(int position) {
        return settings.getAmiiboView();
    }

    @NonNull
    @Override
    public FlaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (VIEW.valueOf(viewType)) {
            case COMPACT:
                return new CompactViewHolder(parent, listener);
            case LARGE:
                return new LargeViewHolder(parent, listener);
            case IMAGE:
                return new ImageViewHolder(parent, listener);
            case SIMPLE:
            default:
                return new SimpleViewHolder(parent, listener);
        }
    }

    @Override
    public void onBindViewHolder(final FlaskViewHolder holder, int position) {
        View highlight = holder.itemView.findViewById(R.id.highlight);
        if (TagMo.getPrefs().flaskActiveSlot().get() == position) {
            highlight.setBackgroundResource(R.drawable.cardview_outline);
        } else {
            highlight.setBackgroundResource(0);
        }
        holder.itemView.setOnClickListener(view -> {
            if (null != holder.listener) {
                holder.listener.onAmiiboClicked(holder.amiibo);
            }
        });
        if (null != holder.imageAmiibo) {
            holder.imageAmiibo.setOnClickListener(view -> {
                if (null != holder.listener) {
                    if (settings.getAmiiboView() == VIEW.IMAGE.getValue())
                        holder.listener.onAmiiboClicked(holder.amiibo);
                    else
                        holder.listener.onAmiiboImageClicked(holder.amiibo);
                }
            });
        }
        holder.bind(getItem(position));
    }

    protected static abstract class FlaskViewHolder extends RecyclerView.ViewHolder {
        private final OnAmiiboClickListener listener;

        public final TextView txtError;
        public final TextView txtName;
        public final TextView txtTagId;
        public final TextView txtAmiiboSeries;
        public final TextView txtAmiiboType;
        public final TextView txtGameSeries;
        // public final TextView txtCharacter;
        public final TextView txtPath;
        public final AppCompatImageView imageAmiibo;

        Amiibo amiibo = null;

        CustomTarget<Bitmap> target = new CustomTarget<>() {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                imageAmiibo.setImageResource(0);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                imageAmiibo.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                imageAmiibo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource, Transition transition) {
                imageAmiibo.setImageBitmap(resource);
                imageAmiibo.setVisibility(View.VISIBLE);
            }
        };

        public FlaskViewHolder(View itemView, OnAmiiboClickListener listener) {
            super(itemView);

            this.listener = listener;

            this.txtError = itemView.findViewById(R.id.txtError);
            this.txtName = itemView.findViewById(R.id.txtName);
            this.txtTagId = itemView.findViewById(R.id.txtTagId);
            this.txtAmiiboSeries = itemView.findViewById(R.id.txtAmiiboSeries);
            this.txtAmiiboType = itemView.findViewById(R.id.txtAmiiboType);
            this.txtGameSeries = itemView.findViewById(R.id.txtGameSeries);
            // this.txtCharacter = itemView.findViewById(R.id.txtCharacter);
            this.txtPath = itemView.findViewById(R.id.txtPath);
            this.imageAmiibo = itemView.findViewById(R.id.imageAmiibo);
        }

        void bind(final Amiibo item) {
            this.amiibo = item;

            String amiiboHexId;
            String amiiboName;
            String amiiboSeries = "";
            String amiiboType = "";
            String gameSeries = "";
            // String character = "";
            String amiiboImageUrl;

            if (null != amiibo) {
                amiiboHexId = TagUtils.amiiboIdToHex(amiibo.id);
                amiiboName = amiibo.name;
                amiiboImageUrl = amiibo.getImageUrl();
                if (null != amiibo.getAmiiboSeries())
                    amiiboSeries = amiibo.getAmiiboSeries().name;
                if (null != amiibo.getAmiiboType())
                    amiiboType = amiibo.getAmiiboType().name;
                if (null != amiibo.getGameSeries())
                    gameSeries = amiibo.getGameSeries().name;

                this.txtError.setVisibility(View.GONE);
                setAmiiboInfoText(txtName, amiiboName);
                setAmiiboInfoText(txtTagId, amiiboHexId);
                setAmiiboInfoText(txtAmiiboSeries, amiiboSeries);
                setAmiiboInfoText(txtAmiiboType, amiiboType);
                setAmiiboInfoText(txtGameSeries, gameSeries);
                // setFoomiiboInfoText(txtCharacter, character);
                this.txtPath.setVisibility(View.GONE);
                if (null != this.imageAmiibo) {
                    GlideApp.with(itemView).clear(target);
                    if (null != amiiboImageUrl) {
                        GlideApp.with(itemView).asBitmap().load(amiiboImageUrl).into(target);
                    }
                }
                if (amiiboHexId.endsWith("00000002") && !amiiboHexId.startsWith("00000000")) {
                    txtTagId.setEnabled(false);
                }
            }
        }

        void setAmiiboInfoText(TextView textView, CharSequence text) {
            textView.setVisibility(View.VISIBLE);
            if (text.length() == 0) {
                textView.setText(R.string.unknown);
                textView.setEnabled(false);
            } else {
                textView.setText(text);
                textView.setEnabled(true);
            }
        }
    }

    static class SimpleViewHolder extends FlaskViewHolder {
        public SimpleViewHolder(ViewGroup parent, OnAmiiboClickListener listener) {
            super(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.amiibo_simple_card, parent, false),
                    listener
            );
        }
    }

    static class CompactViewHolder extends FlaskViewHolder {
        public CompactViewHolder(ViewGroup parent, OnAmiiboClickListener listener) {
            super(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.amiibo_compact_card, parent, false),
                    listener
            );
        }
    }

    static class LargeViewHolder extends FlaskViewHolder {
        public LargeViewHolder(ViewGroup parent, OnAmiiboClickListener listener) {
            super(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.amiibo_large_card, parent, false),
                    listener
            );
        }
    }

    static class ImageViewHolder extends FlaskViewHolder {
        public ImageViewHolder(ViewGroup parent, OnAmiiboClickListener listener) {
            super(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.amiibo_image_card, parent, false),
                    listener
            );
        }
    }

    public interface OnAmiiboClickListener {
        void onAmiiboClicked(Amiibo amiibo);

        void onAmiiboImageClicked(Amiibo amiibo);
    }
}
