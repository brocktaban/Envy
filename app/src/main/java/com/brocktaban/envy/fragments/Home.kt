package com.brocktaban.envy.fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.brocktaban.envy.helpers.Confession
import com.brocktaban.envy.helpers.DataClass
import com.brocktaban.envy.MainActivity
import com.brocktaban.envy.R
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_home.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger

class Home : Fragment(), AnkoLogger {

    private lateinit var mAdapter: Adapter
    private var mList = ArrayList<Confession>()

    private lateinit var db: FirebaseFirestore
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mSwipeToRefresh: SwipeRefreshLayout

    private lateinit var mActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_home, container, false)

        mRecyclerView = v.recyclerView
        mSwipeToRefresh = v.swipe
        mAdapter = Adapter(mList, context)
        db = FirebaseFirestore.getInstance()

        mActivity = activity as MainActivity

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = mAdapter

        getConfession()

        mSwipeToRefresh.setOnRefreshListener { getConfession() }

        return v
    }

    private fun getConfession() = GlobalScope.launch(Dispatchers.Main) {
        mSwipeToRefresh.isRefreshing = true

        val confessions = DataClass.getConfessionAsArrayList(db)

        mList.clear()

        if (confessions != null)
            for (x in confessions)
                mList.add(x)

        mSwipeToRefresh.isRefreshing = false

        mAdapter.notifyDataSetChanged()
    }


    private class Adapter(private val items: ArrayList<Confession>, private val context: Context?) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_home, parent, false)
        )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val confession = items[position]

            Glide.with(context!!).load(confession.image).into(holder.image)

            holder.title.text = confession.title
            holder.content.text = confession.content
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.title
        val content = view.content
        val image = view.image
        val readMore = view.readMore
    }
}
