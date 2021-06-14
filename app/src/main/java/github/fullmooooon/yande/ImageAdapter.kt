package github.fullmooooon.yande

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import com.kennyc.bottomsheet.BottomSheetListener
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment
import okhttp3.*
import okio.IOException
import org.dom4j.DocumentHelper
import org.dom4j.Element
import java.io.File
import java.net.URL


val TAG = "哼哼"

class ImageAdapter(var baseUrl: String, var context: Context) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    val mainActivity = context as MainActivity
    var wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var disWidth = wm.defaultDisplay.width
    var disHeight = wm.defaultDisplay.height
    var page = 0
    var mList: List<Element> = emptyList<Element>()
    var imageLoadCount = 0
    var tags = ""
    lateinit var photoView: PhotoView

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
                        Log.e(TAG, "onResourceReady: imageview click")

                        photoView = mainActivity.findViewById(R.id.photo_view)
                        photoView.setImageBitmap(resource)
//                        Glide.with(context)
//                            .asBitmap()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .load(item.attributeValue("jpeg_url"))
//                            .into(photoView)
                        val fragment: View =
                            (context as MainActivity).findViewById(R.id.fragment_fullscreen)
                        fragment.visibility =
                            View.VISIBLE
                        photoView.setOnClickListener {
                            Log.e(TAG, "onResourceReady: gone")
                            fragment.visibility = View.GONE
                        }

                        myListener.imageAdapter = this@ImageAdapter
                        myListener.itemElement = item
//                        myListener.imageUrl = item.attributeValue("sample_url")
//                        myListener.filename = "yande_id_" + item.attributeValue("id") + ".jpg"
                        photoView.setOnLongClickListener {
                            BottomSheetMenuDialogFragment.Builder(
                                context = context,
                                sheet = R.menu.fragment_fullscreen_dialog,
                                listener = myListener,
                                title = "(=￣ω￣=)",
                                `object` = this
                            )
                                .show(mainActivity.supportFragmentManager)

                            false
                        }
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
                mainActivity.stopRefreshing()
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

    fun downloadImg(item: Element) {
        val imageUrl = item.attributeValue("jpeg_url")
        val filename = "yande_id_" + item.attributeValue("id") + ".jpg"
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    MediaStore.Images.Media.insertImage(
                        context.contentResolver,
                        resource,
                        filename,
                        ""
                    )
                    Toast.makeText(context, "图片保存大成功！", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun loadHd(item: Element) {
        val imageHDUrl = item.attributeValue("jpeg_url")
        Glide.with(context)
            .asBitmap()
            .load(imageHDUrl)
            .into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    photoView.setImageBitmap(resource)
                }
            })

    }
}


object myObject {

}


@SuppressLint("StaticFieldLeak")
object myListener : BottomSheetListener {

    lateinit var imageAdapter: ImageAdapter
    lateinit var itemElement: Element

    override fun onSheetDismissed(
        bottomSheet: BottomSheetMenuDialogFragment,
        `object`: Any?,
        dismissEvent: Int
    ) {

    }

    override fun onSheetItemSelected(
        bottomSheet: BottomSheetMenuDialogFragment,
        item: MenuItem,
        `object`: Any?
    ) {
        when (item.itemId) {
            R.id.BS_download -> {
                Log.e(TAG, "item.itemId==R.id.BS_download")
                imageAdapter.downloadImg(itemElement)
            }
            R.id.BS_loadHD -> {
                imageAdapter.loadHd(itemElement)
            }
        }
    }


    override fun onSheetShown(bottomSheet: BottomSheetMenuDialogFragment, `object`: Any?) {

    }
}
