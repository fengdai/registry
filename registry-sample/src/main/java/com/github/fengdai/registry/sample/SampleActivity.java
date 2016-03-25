package com.github.fengdai.registry.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import com.github.fengdai.registry.RegisterAdapter;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.sample.model.Address;
import com.github.fengdai.registry.sample.model.Card;
import com.github.fengdai.registry.sample.model.Email;
import java.util.Arrays;
import java.util.List;

public class SampleActivity extends AppCompatActivity {
  private List<Object> list =
      Arrays.asList(new Card("Dai feng"), new Address("Abcdefg"), new Address("Abcdefg"),
          new Card("Dai feng", R.mipmap.ic_launcher), new Address("Abcdefg"),
          new Address("Abcdefg"), new Card("Dai feng"), new Address("Abcdefg"),
          new Address("Abcdefg"), new Card("Dai feng"), new Address("Abcdefg"),
          new Address("Abcdefg"), new Card("Dai feng", R.mipmap.ic_launcher),
          new Address("Abcdefg"), new Address("Abcdefg"), new Email("toxic.dai@gmail.com"));

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_activity);
    ListView listView = (ListView) findViewById(android.R.id.list);
    Adapter adapter = new Adapter();
    listView.setAdapter(adapter);
    adapter.notifyDataSetChanged();
  }

  class Adapter extends RegisterAdapter {
    protected Adapter() {
      super(Registry.create(SampleList.class));
    }

    @Override public int getCount() {
      return list.size();
    }

    @Override public Object getItem(int position) {
      return list.get(position);
    }

    @Override public long getItemId(int position) {
      return position;
    }
  }
}
