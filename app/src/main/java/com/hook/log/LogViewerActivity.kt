package com.hook.log

import android.os.Bundle
import android.text.ClipData
import android.text.ClipboardManager
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hook.log.databinding.ActivityLogViewerBinding
import com.hook.log.databinding.ItemLogBinding

class LogViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogViewerBinding
    private val adapter = LogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.rvLogs.apply {
            layoutManager = LinearLayoutManager(this@LogViewerActivity)
            adapter = this@LogViewerActivity.adapter
        }

        registerForContextMenu(binding.rvLogs)

        LogManager.registerObserver { runOnUiThread { adapter.notifyDataSetChanged() } }

        binding.fabClear.setOnClickListener {
            LogManager.clear()
            adapter.notifyDataSetChanged()
            toast("已清空")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogManager.unregisterObserver { adapter.notifyDataSetChanged() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_log_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pause -> {
                LogManager.isPaused = !LogManager.isPaused
                item.title = if (LogManager.isPaused) "继续" else "暂停"
                item.icon = if (LogManager.isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause
                toast(if (LogManager.isPaused) "已暂停" else "已继续")
                true
            }
            R.id.action_clear -> {
                LogManager.clear()
                adapter.notifyDataSetChanged()
                toast("已清空")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.add(0, 1, 0, "复制内容")
        menu?.add(0, 2, 1, "复制完整JSON")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as android.widget.AdapterView.AdapterContextMenuInfo
        val text = adapter.getItem(info.position)?.content ?: ""
        return when (item.itemId) {
            1, 2 -> { copyToClipboard(text); true }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip = ClipData.newPlainText("HookLog", text)
        toast("已复制到剪贴板")
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    inner class LogAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<LogAdapter.VH>() {
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
            VH(ItemLogBinding.inflate(layoutInflater, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(LogManager.getLogs()[position])
        }

        override fun getItemCount() = LogManager.getLogs().size

        fun getItem(position: Int) = LogManager.getLogs()[position]

        inner class VH(private val binding: ItemLogBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
            fun bind(item: LogManager.LogItem) {
                binding.tvTime.text = item.time
                binding.tvContent.text = item.content
            }
        }
    }
}
