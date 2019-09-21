package com.github.fengdai.registry.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.fengdai.registry.RegistryListAdapter;
import com.github.fengdai.registry.Utils;
import com.github.fengdai.registry.ViewHolderFactory;
import com.github.fengdai.registry.sample.SampleListRegistry.Item;
import com.github.fengdai.registry.sample.holder.CardVH;
import com.github.fengdai.registry.sample.holder.TextViewVH;
import com.github.fengdai.registry.sample.model.Address;
import com.github.fengdai.registry.sample.model.Card;
import com.github.fengdai.registry.sample.model.Email;
import com.github.fengdai.registry.sample.model.Head;
import java.util.Arrays;
import java.util.List;

import static com.github.fengdai.registry.sample.SampleListRegistry.itemOf;
import static com.github.fengdai.registry.sample.SampleListRegistry.itemOf_CardBinder_TextOnly;
import static com.github.fengdai.registry.sample.SampleListRegistry.itemOf_CardVH;
import static com.github.fengdai.registry.sample.SampleListRegistry.itemOf_gap;

public class SampleActivity extends AppCompatActivity {
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
    SampleListRegistry registry = new SampleListRegistry(
        new ViewHolderFactory<CardVH>() {
          @Override public CardVH create(ViewGroup parent) {
            return new CardVH(
                Utils.<LinearLayout>inflate(parent, android.R.layout.activity_list_item));
          }
        },
        new ViewHolderFactory<TextViewVH>() {
          @Override public TextViewVH create(ViewGroup parent) {
            return new TextViewVH(
                Utils.<TextView>inflate(parent, R.layout.simple_list_item_1));
          }
        });
    RegistryListAdapter<SampleListRegistry.Item> adapter =
        new RegistryListAdapter<>(registry, new BadDiffCallback());
    listView.setAdapter(adapter);
    adapter.submitList(list);
  }

  // Do NOT do like this in real world.
  private static class BadDiffCallback extends DiffUtil.ItemCallback<SampleListRegistry.Item> {
    @Override public boolean areItemsTheSame(@NonNull SampleListRegistry.Item oldItem,
        @NonNull SampleListRegistry.Item newItem) {
      return false;
    }

    @Override public boolean areContentsTheSame(@NonNull SampleListRegistry.Item oldItem,
        @NonNull SampleListRegistry.Item newItem) {
      return false;
    }
  }
}
