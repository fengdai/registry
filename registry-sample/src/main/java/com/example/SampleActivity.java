package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.github.fengdai.registry.AdapterDelegate;
import com.github.fengdai.registry.RegistryListAdapter;
import java.util.Arrays;

public class SampleActivity extends Activity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_activity);
    RecyclerView listView = findViewById(android.R.id.list);
    final LayoutInflater layoutInflater = getLayoutInflater();
    View.OnClickListener barOnClickListener =
        v -> Toast.makeText(this, "Bar's itemView has been clicked", Toast.LENGTH_SHORT).show();
    AdapterDelegate<SampleRegistry.Item> adapterDelegate = new SampleRegistry_Impl.AdapterDelegate(
        parent -> new BarViewHolder(layoutInflater, parent, barOnClickListener),
        parent -> new FooViewHolder(layoutInflater, parent));
    RegistryListAdapter<SampleRegistry.Item> adapter =
        new RegistryListAdapter<>(adapterDelegate, new DiffCallback());
    listView.setAdapter(adapter);
    SampleRegistry sampleRegistry = new SampleRegistry_Impl();
    adapter.submitList(Arrays.asList(
        sampleRegistry.fooItem(new Foo("Foo")),
        sampleRegistry.barItem(new Bar("Bar")),
        sampleRegistry.footerItem()
    ));
  }

  private static class DiffCallback extends DiffUtil.ItemCallback<SampleRegistry.Item> {
    @Override public boolean areItemsTheSame(@NonNull SampleRegistry.Item oldItem,
        @NonNull SampleRegistry.Item newItem) {
      return oldItem == newItem;
    }

    @Override public boolean areContentsTheSame(@NonNull SampleRegistry.Item oldItem,
        @NonNull SampleRegistry.Item newItem) {
      return false;
    }
  }
}
