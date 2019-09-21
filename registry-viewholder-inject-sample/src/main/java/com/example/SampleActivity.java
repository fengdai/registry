package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;
import com.example.SampleList_Registry.Item;
import com.example.model.Address;
import com.example.model.Card;
import com.example.model.Email;
import com.example.model.Head;
import com.github.fengdai.registry.RegistryListAdapter;
import java.util.Arrays;
import java.util.List;

import static com.example.SampleList_Registry.itemOf;
import static com.example.SampleList_Registry.itemOf_CardBinder_TextOnly;
import static com.example.SampleList_Registry.itemOf_CardVH;
import static com.example.SampleList_Registry.itemOf_gap;

public class SampleActivity extends Activity {
  private List<Item> list = Arrays.asList(
      itemOf(new Head("Registry Sample", android.R.drawable.ic_popup_sync)),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf_CardVH(new Card("Android Developer", R.mipmap.ic_launcher)),
      itemOf_CardBinder_TextOnly(new Card("Feng Dai")),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap()
  );

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_activity);
    RecyclerView listView = findViewById(android.R.id.list);
    SampleList_Registry registry = DaggerSampleListComponent.factory()
        .create(v -> Toast.makeText(this, "TextViewVH's itemView clicked", Toast.LENGTH_SHORT).show())
        .registry();
    RegistryListAdapter<SampleList_Registry.Item> adapter =
        new RegistryListAdapter<>(registry, new DiffCallback());
    listView.setAdapter(adapter);
    adapter.submitList(list);
  }

  private static class DiffCallback extends DiffUtil.ItemCallback<SampleList_Registry.Item> {
    @Override public boolean areItemsTheSame(@NonNull SampleList_Registry.Item oldItem,
        @NonNull SampleList_Registry.Item newItem) {
      return oldItem == newItem;
    }

    @Override public boolean areContentsTheSame(@NonNull SampleList_Registry.Item oldItem,
        @NonNull SampleList_Registry.Item newItem) {
      return false;
    }
  }
}
