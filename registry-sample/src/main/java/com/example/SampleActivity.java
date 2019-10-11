package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.example.holder.CardVH;
import com.example.holder.TextViewVH;
import com.example.model.Card;
import com.example.model.Head;
import com.example.model.Location;
import com.github.fengdai.registry.RegistryListAdapter;
import java.util.Arrays;
import java.util.List;

public class SampleActivity extends Activity {
  private List<Object> list = Arrays.asList(
      new Head("Registry Sample", android.R.drawable.ic_popup_sync),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP,
      new Card("Android Developer", R.mipmap.ic_launcher),
      new Card("Feng Dai"),
      new Location("Hangzhou, China"),
      SampleList.GAP
  );

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_activity);
    RecyclerView listView = findViewById(android.R.id.list);
    final LayoutInflater layoutInflater = getLayoutInflater();
    SampleList_Registry registry = new SampleList_Registry(
        parent -> new CardVH(layoutInflater.inflate(android.R.layout.activity_list_item, parent, false)),
        parent -> new TextViewVH((TextView) layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)));
    RegistryListAdapter<Object> adapter = new RegistryListAdapter<>(registry, new DiffCallback());
    listView.setAdapter(adapter);
    adapter.submitList(list);
  }

  private static class DiffCallback extends DiffUtil.ItemCallback<Object> {
    @Override public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
      return oldItem == newItem;
    }

    @Override public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
      return false;
    }
  }
}
