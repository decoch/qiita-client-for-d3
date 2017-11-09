package com.example.tominaga.qiita_client_d3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class ListActivity : Activity() {

    lateinit private var mAdapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        mAdapter = ListAdapter(this, R.layout.list_item)

        val listView = findViewById<ListView>(R.id.list_view) as ListView
        listView.adapter = mAdapter

        val editText = findViewById<EditText>(R.id.edit_text) as EditText
        editText.setOnKeyListener(OnKeyListener())

        listView.onItemClickListener = OnItemClickListener()
    }

    private inner class ListAdapter(context: Context, resource: Int) : ArrayAdapter<Item>(context, resource) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                // 再利用可能なViewがない場合は作る
                convertView = layoutInflater.inflate(R.layout.list_item, null)
            }

            val imageView = convertView?.findViewById<ImageView>(R.id.image_view) as ImageView
            val itemTitleView = convertView.findViewById<TextView>(R.id.item_title) as TextView
            val userNameView = convertView.findViewById<TextView>(R.id.user_name) as TextView

            imageView.setImageBitmap(null) // 残ってる画像を消す（再利用された時）

            // 表示する行番号のデータを取り出す
            val result = getItem(position)

            Picasso.with(context).load(result.user?.profile_image_url).into(imageView)
            itemTitleView.text = result.title
            userNameView.text = result.user?.name

            return convertView
        }
    }

    private inner class OnKeyListener : View.OnKeyListener {

        override fun onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean {
            if (keyEvent.action != KeyEvent.ACTION_UP || keyCode != KeyEvent.KEYCODE_ENTER) {
                return false
            }

            val editText = view as EditText
            // キーボードを閉じる
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)

            var text = editText.text.toString()
            try {
                // url encode
                text = URLEncoder.encode(text, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                Log.e("", e.toString(), e)
                return true
            }

            if (!TextUtils.isEmpty(text)) {
                val request = QiitaClient.create().items(text)
                Log.d("", request.request().url().toString())
                val item = object : Callback<List<Item>> {
                    override fun onResponse(call: Call<List<Item>>?, response: Response<List<Item>>?) {
                        mAdapter.clear()
                        response?.body()?.forEach { mAdapter.add(it) }
                    }

                    override fun onFailure(call: Call<List<Item>>?, t: Throwable?) {
                    }
                }
                request.enqueue(item)
            }
            return true
        }
    }


    private inner class OnItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val intent = Intent(this@ListActivity, DetailActivity::class.java)
            // タップされた行番号のデータを取り出す
            val result = mAdapter.getItem(position)
            intent.putExtra("url", result.url)
            startActivity(intent)
        }
    }
}
