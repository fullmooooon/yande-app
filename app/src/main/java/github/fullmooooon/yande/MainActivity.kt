package github.fullmooooon.yande

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.*
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.mancj.materialsearchbar.MaterialSearchBar
import okhttp3.*
import org.dom4j.Element


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    MaterialSearchBar.OnSearchActionListener, PopupMenu.OnMenuItemClickListener {

//    class FullScreenFragment : Fragment(R.layout.fragment_fullscreen)

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var imageAdapter: ImageAdapter
    private var lastSearches: List<String>? = null
    private var searchBar: MaterialSearchBar? = null
    var searchText: CharSequence = ""
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var fragment_fullscreen: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        fragment_fullscreen = findViewById(R.id.fragment_fullscreen)
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh)
        mSwipeRefreshLayout.setColorSchemeResources(
            R.color.blue,
            R.color.purple,
            R.color.green,
            R.color.orange
        )
        mSwipeRefreshLayout.setOnRefreshListener {
            resetImageAdapter()
        }
//        val fragment: FragmentContainerView = findViewById(R.id.fragment_fullscreen)
//        fragment.setOnClickListener {
//            Log.e(TAG, "onCreate: fragmentclick", )
//
//        }


//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        imageAdapter = ImageAdapter("https://oreno.imouto.us/", this)
        val recyclerView: RecyclerView = findViewById(R.id.rv)
        recyclerView.itemAnimator = null
        runOnUiThread {
            recyclerView.adapter = imageAdapter
            val staggeredGridLayoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            val linearLayoutManager = LinearLayoutManager(this@MainActivity)
            val gridLayoutManager =
                GridLayoutManager(this@MainActivity, 2, GridLayoutManager.VERTICAL, false)
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            imageAdapter.loadNextPage()
//            recyclerView.addOnScrollListener(RecyclerView.)
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)) {
                        imageAdapter.loadNextPage()
                    }
                }
            })
            recyclerView.recycledViewPool.setMaxRecycledViews(0, 20)
            recyclerView.setDrawingCacheEnabled(true);
        }

        searchBar = findViewById(R.id.searchBar) as MaterialSearchBar
        searchBar!!.setHint("Custom hint")
        searchBar!!.setSpeechMode(true)
        searchBar!!.setOnSearchActionListener(this)
        searchBar!!.inflateMenu(R.menu.main)
        searchBar!!.getMenu().setOnMenuItemClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onDestroy() {
//        saveSearchSuggestionToDisk(searchBar.getLastSuggestions());
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        val s = if (enabled) "enabled" else "disabled"
//        Toast.makeText(this@MainActivity, "Search $s", Toast.LENGTH_SHORT).show()
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        Log.e(TAG, "onSearchConfirmed: ${text}")
        if (text != null) {
            searchText = text
        }
        resetImageAdapter()
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    override fun onButtonClicked(buttonCode: Int) {
        return
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return true
    }

    fun resetImageAdapter() {
        imageAdapter.page = 0
        imageAdapter.tags = searchText.toString()
        imageAdapter.mList = emptyList<Element>()
        imageAdapter.loadNextPage()
    }

    fun stopRefreshing() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    fun fullscreenFragmentShow() {
        fragment_fullscreen.visibility =
            View.VISIBLE
    }

    fun fullscreenFragmentHidden() {
        fragment_fullscreen.visibility =
            View.GONE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fragment_fullscreen.visibility ==
                View.VISIBLE
            ) {
                this.fullscreenFragmentHidden()
                return true;
            } else {
                return super.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}