package com.brocktaban.envy

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_main_menu_list_dialog.*
import kotlinx.android.synthetic.main.fragment_menu_dialog_item.view.*


class MainMenuModal(private val mList: Array<String>, private val mIcons: ArrayList<Int>) : BottomSheetDialogFragment() {
    private var mListener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = LinearLayoutManager(context)
        list.adapter = MenuItemAdapter(mList, mIcons)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        mListener = if (parent != null) {
            parent as Listener
        } else {
            context as Listener
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    interface Listener {
        fun onMenuItemClicked(position: Int)
    }

    private inner class ViewHolder(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_menu_dialog_item, parent, false)) {

        internal val text: TextView = itemView.text
        internal val icon: ImageView = itemView.icon

        init {
            text.setOnClickListener {
                mListener?.let {
                    it.onMenuItemClicked(adapterPosition)
                    dismiss()
                }
            }
        }
    }

    private inner class MenuItemAdapter(private val list: Array<String>, private val icons: ArrayList<Int>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = list[position]
            holder.icon.setImageResource(icons[position])
        }

        override fun getItemCount() = list.size
    }
}
