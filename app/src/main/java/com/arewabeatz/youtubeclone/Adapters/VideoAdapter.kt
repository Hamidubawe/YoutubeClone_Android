package com.arewabeatz.youtubeclone.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.Models.UserModel
import com.arewabeatz.youtubeclone.R
import com.arewabeatz.youtubeclone.VideoPlayerActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class VideoAdapter(val context: Context, private val  list: List<PostModel>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DEFAULT_VIEW_TYPE = 1
    private val NATIVE_AD_VIEW_TYPE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == DEFAULT_VIEW_TYPE){

            val v = LayoutInflater.from(context).inflate(R.layout.video_layout, parent, false)

            return VideoHolder(v)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.native_ad_layout, parent, false)
            NativeAdHolder(view, context)
        }


    }

    override fun getItemCount(): Int {

        return list.size
    }

    override fun getItemViewType(position: Int): Int {

        return if ((position + 1) %3 == 0 && (position + 1) != 0) {
            NATIVE_AD_VIEW_TYPE
        }else
            DEFAULT_VIEW_TYPE
    }

    override fun onBindViewHolder(mHolder: RecyclerView.ViewHolder, position: Int) {

        if(mHolder.itemViewType == DEFAULT_VIEW_TYPE){

            val holder : VideoHolder = mHolder as VideoHolder

            val video = list[position]

            try {
                Picasso.get().load(video.videoThumbnail).placeholder(R.drawable.ic_play).into(holder.videoThumbNail)
            }catch (e:Exception){
                holder.videoThumbNail.setImageResource(R.drawable.ic_play)
            }
            holder.videoTitle.text = video.videoTitle
            holder.videoDate.text = video.videoDate
            getPublisher(holder.channelName, video.publisher, holder.videoPublisherPhoto)
            getPlayedTimes(position, holder.videoViews)


            holder.itemView.setOnClickListener {

                if (FirebaseAuth.getInstance().currentUser != null) {
                    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
                    FirebaseDatabase.getInstance().reference
                        .child("Played").child(currentUser).child(video.videoId)
                        .setValue("played")
                }
                FirebaseDatabase.getInstance().reference.child("Plays")
                    .child(video.videoId).push().setValue("played")

                val i = Intent(context, VideoPlayerActivity::class.java)
                i.putExtra("videoId", video.videoId)
                context.startActivity(i)
            }
        }

    }

    private fun getPlayedTimes(position: Int, play: TextView) {

        val ref = FirebaseDatabase.getInstance().reference.child("Plays")
            .child(list[position].videoId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val playCount = snapshot.childrenCount.toInt()
                    play.text = "${playCount.toString()} views"
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

    }

    private fun getPublisher(videoPublisher: TextView, publisher: String, publisherPhoto: CircleImageView) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(publisher)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)

                videoPublisher.text = user!!.name
                try {
                    Picasso.get().load(user.profilePhoto).into(publisherPhoto)
                }catch (e:Exception){
                    publisherPhoto.setImageResource(R.drawable.ic_play)
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }


    class VideoHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val videoThumbNail: ImageView = itemView.findViewById(R.id.videoThumbNail)
        val videoTitle: TextView = itemView.findViewById(R.id.videoTitle)
        val videoPublisherPhoto: CircleImageView = itemView.findViewById(R.id.videoPublisherPhoto)
        val channelName:TextView = itemView.findViewById(R.id.channelName)
        val videoDate:TextView = itemView.findViewById(R.id.videoDate)
        val videoViews:TextView = itemView.findViewById(R.id.videoViews)
    }

    class NativeAdHolder(view: View, context: Context): RecyclerView.ViewHolder(view){

        private lateinit var template: TemplateView
        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                // Show the ad.
                val styles = NativeTemplateStyle.Builder()
                    .withMainBackgroundColor(ColorDrawable(Color.BLACK)).build()


                template = view.findViewById(R.id.my_template)
                template.setStyles(styles)
                template.setNativeAd(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {

                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    AdRequest.Builder().build()
                }

                override fun onAdLoaded() {

                    template.visibility = View.VISIBLE

                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()

            .loadAd(AdRequest.Builder().build())


    }

}