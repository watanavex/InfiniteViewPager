package tech.watanave.infinitepager

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, CollectionDemoFragment())
            .commit()
    }
}

class CollectionDemoFragment : Fragment(R.layout.collection_demo) {
    private lateinit var demoCollectionAdapter: DemoCollectionAdapter
    private lateinit var viewPager: ViewPager2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        demoCollectionAdapter = DemoCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        val adapter = TabAdapter()
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = layoutManager

        val snapHelper = SnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        adapter.itemTapBlock = { position ->
            viewPager.setCurrentItem(position, true)
        }

        val gestureDetector = GestureDetector(requireContext(), object: GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val view = recyclerView.findChildViewUnder(e.x, e.y)!!
                val position = recyclerView.getChildAdapterPosition(view)
                viewPager.setCurrentItem(position, true)
                return super.onSingleTapUp(e)
            }
        })
        recyclerView.addOnItemTouchListener(object: RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                gestureDetector.onTouchEvent(e)
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val smoothScroller = snapHelper.getScroller(layoutManager)!!
                smoothScroller.targetPosition = position

                layoutManager.startSmoothScroll(smoothScroller)
            }
        })

        layoutManager.scrollToPosition(Int.MAX_VALUE / 2)
        viewPager.setCurrentItem(Int.MAX_VALUE / 2, false)
    }
}

class SnapHelper : PagerSnapHelper() {

    var onSnapViewBlock: ((Int) -> Unit)? = null

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int {
        val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        onSnapViewBlock?.invoke(position)
        return position
    }

    fun getScroller(layoutManager: RecyclerView.LayoutManager) : RecyclerView.SmoothScroller? {
        return this.createScroller(layoutManager)
    }

}

class TabAdapter : RecyclerView.Adapter<TabAdapter.ViewHolder>() {

    var itemTapBlock: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tab_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val value = position % 3
        val button = holder.button

        button.text = "Object $value"
        button.setOnClickListener {
            itemTapBlock?.invoke(position)
        }
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    class ViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button
            get() = itemView.findViewById(R.id.button)
    }
}

class DemoCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val fragment = DemoObjectFragment()
        val value = position % 3
        fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT, value)
        }
        return fragment
    }
}

private const val ARG_OBJECT = "object"

class DemoObjectFragment : Fragment(R.layout.fragment_collection_object) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            val textView: TextView = view.findViewById(R.id.text_view)
            textView.text = getInt(ARG_OBJECT).toString()
        }
    }
}
