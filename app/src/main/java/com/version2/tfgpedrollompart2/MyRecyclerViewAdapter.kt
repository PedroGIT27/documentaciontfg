
//noinspection SuspiciousImport
import com.version2.tfgpedrollompart2.R
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.version2.tfgpedrollompart2.Message

class MyRecyclerViewAdapter internal constructor(
    context: Context?,
    private val messages: List<Message>,
    private val currentUser:String
) :
    RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
  //  private var mClickListener: ItemClickListener? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.layout_message, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val message = messages[position]
        holder.messageText.text = message.message

val params=holder.messageText.layoutParams as ViewGroup.MarginLayoutParams

        if(message.sender==currentUser){

            holder.messageText.textAlignment=View.TEXT_ALIGNMENT_VIEW_END
            holder.messageText.setBackgroundResource(R.drawable.message_right)
            holder.messageText.setTextColor(Color.BLACK)
            params.marginStart=100
            params.marginEnd=0


        }else{

            holder.messageText.textAlignment=View.TEXT_ALIGNMENT_VIEW_START
            holder.messageText.setBackgroundResource(R.drawable.message_left)
            holder.messageText.setTextColor(Color.BLACK)
            params.marginStart=0
            params.marginEnd=100

        }
        holder.messageText.layoutParams = params

    }

    // total number of rows
    override fun getItemCount(): Int {
        return messages.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView){

        var messageText: TextView = itemView.findViewById(R.id.messagetxt)

    }

}
//De momento no la voy a usar, la he dejado aquí porque el modelo original la empleaba.
/*class ItemClickListener {
*
*}
*/