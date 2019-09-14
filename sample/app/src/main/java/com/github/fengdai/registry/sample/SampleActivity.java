package com.github.fengdai.registry.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import com.github.fengdai.registry.RegistryAdapter;
import com.github.fengdai.registry.sample.SampleListRegistry.Item;
import com.github.fengdai.registry.sample.holder.CardVH;
import com.github.fengdai.registry.sample.lib.holder.TextViewVH;
import com.github.fengdai.registry.sample.model.Address;
import com.github.fengdai.registry.sample.model.Card;
import com.github.fengdai.registry.sample.model.Email;
import com.github.fengdai.registry.sample.model.Head;
import java.util.Arrays;
import java.util.List;

import static com.github.fengdai.registry.sample.SampleListRegistry.itemOf;
import static com.github.fengdai.registry.sample.SampleListRegistry.itemOf_gap;

public class SampleActivity extends AppCompatActivity {
  private List<Item> list = Arrays.asList(
      itemOf(new Head("Registry Sample", android.R.drawable.ic_popup_sync)),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap(),
      itemOf(new Card("Android Developer", R.mipmap.ic_launcher), CardVH.class),
      itemOf(new Card("Feng Dai"), TextViewVH.class),
      itemOf(new Email("toxic.dai@gmail.com")),
      itemOf(new Address("Hangzhou, China")),
      itemOf_gap()
  );

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_activity);
    RecyclerView listView = findViewById(android.R.id.list);
    Adapter adapter = new Adapter();
    listView.setAdapter(adapter);
    adapter.notifyDataSetChanged();
  }

  class Adapter extends RegistryAdapter<SampleListRegistry.Item> {
    Adapter() {
      super(new SampleListRegistry());
    }

    @Override public int getItemCount() {
      return list.size();
    }

    @Override public Item getItem(int position) {
      return list.get(position);
    }
  }
}
