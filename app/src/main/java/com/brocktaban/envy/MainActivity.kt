package com.brocktaban.envy

import android.os.Bundle
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor

class MainActivity : _Main(), AnkoLogger, MainMenuModal.Listener {

    override fun onMenuItemClicked(position: Int) {
        when (position) {
            0 -> {}
        }
    }

    private var fabCenter = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mUser == null)
            startActivity(intentFor<Auth>())
        else
            setContentView(R.layout.activity_main)

        setSupportActionBar(bar)

        fab.setOnClickListener {
            if (fabCenter) {
                bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
                fab.setImageResource(R.drawable.ic_done_black_24dp)
            } else {
                bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                fab.setImageResource(R.drawable.ic_add_black_24dp)
            }

            fabCenter = !fabCenter
        }

        bar.setNavigationOnClickListener {
            val menuList: Array<String> = arrayOf("Account", "Search", "Setting")
            val menuIcons = ArrayList<Int>()

            menuIcons.add(R.drawable.ic_person_black_24dp)
            menuIcons.add(R.drawable.ic_search_black_24dp)
            menuIcons.add(R.drawable.ic_settings_black_24dp)

            MainMenuModal(menuList, menuIcons).show(supportFragmentManager, "main menu") }
    }
}
