package github.fullmooooon.yande

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import okhttp3.*
import okio.IOException
import org.dom4j.DocumentHelper
import org.dom4j.Element
import java.util.regex.Pattern


val TAG = "哼哼"

class ImageAdapter(var baseUrl: String, var context: Context) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    var wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var disWidth = wm.defaultDisplay.width
    var disHeight = wm.defaultDisplay.height
    var page = 0
    var mList: List<Element> = emptyList<Element>()
    var imageLoadCount = 0
    var tags = ""

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var ivIcon: ImageView

        init {
            ivIcon = itemView.findViewById(R.id.magicItemImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_list_view, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.e(TAG, "onBindViewHolder: ${holder}")
        val item = mList[position]
        var imageView: ImageView = holder.ivIcon
        imageView.visibility = View.INVISIBLE
        imageView.resources
        val url = item.attributeValue("sample_url")
        Glide.with(context)
            .asBitmap()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(item.attributeValue("sample_url"))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    Log.e(TAG, "onResourceReady: ${++imageLoadCount}")
                    val w = resource.getWidth()
                    val h = resource.getHeight()
                    var _resource = Bitmap.createScaledBitmap(
                        resource, disWidth / 2,
                        (h.toDouble() / w * disWidth / 2).toInt(), false
                    );
                    imageView.setImageBitmap(_resource)
                    imageView.visibility = View.VISIBLE
                    imageView.setOnClickListener {
                        Log.e(TAG, "onResourceReady: imageview click", )
                        val mainActivity = context as MainActivity
                        val photoView : PhotoView = mainActivity.findViewById(R.id.photo_view)
                        photoView.setImageBitmap(_resource)
                        val fragment: View = (context as MainActivity).findViewById(R.id.fragment_fullscreen)
                        if (fragment.visibility == View.GONE) fragment.visibility =
                            View.VISIBLE
                        else fragment.visibility = View.GONE
                        fragment.bringToFront()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // this is called when imageView is cleared on lifecycle call or for
                    // some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView
                    // clear it here as you can no longer have the bitmap
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                }
            })
//        Picasso.get()
//            .load(url)
//            .into(imageView)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    private val client = OkHttpClient()

    fun loadNextPage() {
        page++
        val url = "${baseUrl}post.xml?page=${page}&tags=${tags}"
        Log.e(TAG, "loadNextPage: ${url}")
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("\\033[0m" + e + "\\033[0m")
                call.clone().execute()
            }

            override fun onResponse(call: Call, response: Response) {
                val html = response.body!!.string()
                var document = DocumentHelper.parseText(html)
                document.rootElement.elements("post")
                mList = mList.plus(document.rootElement.elements("post"))
                Log.e(TAG, "onResponse: ${mList.size}")
                runOnUiThread {
                    notifyDataSetChanged()
                }
            }
        })
    }

    private val handler = Handler(Looper.getMainLooper())
    fun runOnUiThread(action: () -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(action)
        } else {
            action.invoke()
        }
    }

}