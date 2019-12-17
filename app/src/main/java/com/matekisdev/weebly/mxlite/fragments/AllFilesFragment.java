package com.matekisdev.weebly.mxlite.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.matekisdev.weebly.mxlite.R;
import com.matekisdev.weebly.mxlite.VideoActivity;
import com.matekisdev.weebly.mxlite.database.DatabaseHelper;
import com.matekisdev.weebly.mxlite.database.Mp4FileEntity;
import com.matekisdev.weebly.mxlite.database.Mp4SrtDatabase;
import com.matekisdev.weebly.mxlite.misc.FormatterFunctions;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllFilesFragment extends Fragment
{
    private static WeakReference<TextView> noFilesWarning;
    private static WeakReference<ProgressBar> progressBar;
    private static WeakReference<SpinnerSorter> ss;
    private static RecyclerView rv;
    private static ListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noFilesWarning = new WeakReference<>(view.findViewById(R.id.tvNoFiles));



        rv = view.findViewById(R.id.rvAll);
        rv.setHasFixedSize(true);
        adapter = new ListAdapter();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        ss = new WeakReference<>(new SpinnerSorter(view.findViewById(R.id.spSortingSpinner)));
        ss.get().setUpSorting();

        progressBar = new WeakReference<>(view.findViewById(R.id.pbLoadingCircle));

        Button refreshButton = view.findViewById(R.id.bRefresh);
        refreshButton.setOnClickListener(v -> {

            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.button_rotate_anim));
            new AsyncLoadFiles(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });

        new AsyncLoadFiles(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class AsyncLoadFiles extends AsyncTask<Void, Void, Void>
    {
        private boolean refreshPurpose;

        AsyncLoadFiles(boolean refreshPurpose)
        {
            this.refreshPurpose = refreshPurpose;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rv.setVisibility(View.INVISIBLE);
            progressBar.get().setVisibility(View.VISIBLE);

            adapter.data.clear();
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            // A user kérheti a videó lista frissítését
            if (refreshPurpose)
            {
                DatabaseHelper.getInstance().updateDatabase();
            }
            // Mert, hogy amúgy ez csak első indításra történne meg.
            // Így nem kell sokezerszer bejárni a fájlrendszert, csak ha a user hiányolja a fájlt
            else
            {
                DatabaseHelper.getInstance().updateIfNeeded(progressBar.get().getContext());
            }

            Mp4SrtDatabase database = DatabaseHelper.getInstance().getDb();
            ArrayList<Mp4FileEntity> entities = new ArrayList<>(database.getDao().getEveryMp4File());

            adapter.data.addAll(entities);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            rv.setVisibility(View.VISIBLE);
            progressBar.get().setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            ss.get().sort();

            if (adapter.data.size() == 0 && noFilesWarning.get() != null)
            {
                noFilesWarning.get().setVisibility(View.VISIBLE);
            }
            else if (noFilesWarning.get() != null)
            {
                noFilesWarning.get().setVisibility(View.GONE);
            }
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>
    {
        private List<Mp4FileEntity> data = new ArrayList<>();

        @NonNull
        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_videos, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position)
        {
            holder.init(position, data.get(position));
        }

        @Override
        public int getItemCount()
        {
            return data.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            int position;

            private ConstraintLayout itemItself;
            private ImageView itemPreview;
            private TextView itemName;
            private TextView itemDuration;
            private TextView itemSize;
            private ImageView itemSrtLogo;      // ha van srt akkor legyen egy + imageview


            void init(int pos, Mp4FileEntity file)
            {
                position = pos;

                Glide.with(itemPreview).load(Uri.fromFile(new File(file.path))).into(itemPreview);

                itemName.setText(file.name);
                itemDuration.setText(file.duration);
                itemSize.setText(file.size);
                itemSrtLogo.setVisibility(file.hasSrt ? View.VISIBLE : View.GONE);
            }

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                itemItself = itemView.findViewById(R.id.clItem);
                itemPreview = itemView.findViewById(R.id.ivItemPreview);
                itemName = itemView.findViewById(R.id.tvItemName);
                itemDuration = itemView.findViewById(R.id.tvItemDuration);
                itemSize = itemView.findViewById(R.id.tvItemSize);
                itemSrtLogo = itemView.findViewById(R.id.ivItemSrtLogo);

                itemItself.setOnClickListener(v -> {

                    Intent intent = new Intent(getContext(), VideoActivity.class);
                    intent.putExtra("path", data.get(position).path);       // ezt a videót akarjuk megnyitni
                    intent.putExtra("hasSrt", data.get(position).hasSrt);   // átadjuk, hogy keressünk-e szöveget
                    intent.putExtra("progress", 0);                   // ha az allFiles-ból nyitunk akkor a nulláról kezdünk
                    startActivity(intent);
                });

                itemItself.setOnCreateContextMenuListener((menu, v, menuInfo) -> menu.add("Delete").setOnMenuItemClickListener(item -> {

                    File fileToDelete = new File(data.get(position).path);
                    DatabaseHelper.getInstance().deleteFile(data.get(position));
                    fileToDelete.delete();
                    data.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());

                    if (getItemCount() == 0)
                    {
                        noFilesWarning.get().setVisibility(View.VISIBLE);
                    }

                    return true;
                }));
            }
        }
    }



    private class SpinnerSorter implements AdapterView.OnItemSelectedListener
    {
        private Spinner spinner;
        private SharedPreferences sp;

        SpinnerSorter(Spinner spinner)
        {
            this.spinner = spinner;
            this.sp = spinner.getContext().getSharedPreferences("spinner_sp", Context.MODE_PRIVATE);
        }

        void setUpSorting()
        {
            String sortOptions[] = {" Alphabetically ", " By size ", " By duration "};
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(spinner.getContext(), R.layout.support_simple_spinner_dropdown_item, sortOptions);
            spinner.setAdapter(spinnerAdapter);

            spinner.setOnItemSelectedListener(this);

            // Mi volt az utolsó sortolási mód?
            int startPos = sp.getInt("sort_type", 0);
            spinner.setSelection(startPos, true);
        }

        // refresh után szükség lehet rá
        void sort()
        {
            int position = spinner.getSelectedItemPosition();

            // alphabetical sort
            if (position == 0)
            {
                Collections.sort(adapter.data, (Mp4FileEntity m1, Mp4FileEntity m2) -> m1.name.compareTo(m2.name));
            }
            // sort by size
            else if (position == 1)
            {
                Collections.sort(adapter.data, (Mp4FileEntity m1, Mp4FileEntity m2) -> Long.compare(FormatterFunctions.sizeFormatToLong(m2.size), FormatterFunctions.sizeFormatToLong(m1.size)));
            }
            // sort by duration
            else
            {
                Collections.sort(adapter.data, (Mp4FileEntity m1, Mp4FileEntity m2) -> Integer.compare(FormatterFunctions.dateFormatToInt(m2.duration), FormatterFunctions.dateFormatToInt(m1.duration)));
            }
        }

        // Ez elég gáz, nem igazán bővíthető
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("sort_type", position);
            editor.apply();

            sort();

            adapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {
            // don't care
        }
    }
}
