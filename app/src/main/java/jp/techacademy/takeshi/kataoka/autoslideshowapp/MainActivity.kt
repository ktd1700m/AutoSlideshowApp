package jp.techacademy.takeshi.kataoka.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSHIONS_REQUEST_CODE = 100

    private var mCursor: Cursor? = null
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSHIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSHIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        mCursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート（null：ソートなし）
        )

        // 初期画像の表示
        if (mCursor!!.moveToFirst()) {
            setImageInfo(mCursor!!, imageView1)
        }

        // 進むボタン
        buttonNext.setOnClickListener {
            if (mCursor!!.moveToNext()) {
                setImageInfo(mCursor!!, imageView1)
            } else {
                mCursor!!.moveToFirst()
                setImageInfo(mCursor!!, imageView1)
            }
        }

        // 戻るボタン
        buttonPrev.setOnClickListener {
            if (mCursor!!.moveToPrevious()) {
                setImageInfo(mCursor!!, imageView1)
            } else {
                mCursor!!.moveToLast()
                setImageInfo(mCursor!!, imageView1)
            }
        }

        // 再生・停止ボタン
        buttonPauseRestart.setOnClickListener {
            if (mTimer == null) {
                // ボタンの表示変更
                buttonPauseRestart.text = "停止"
                buttonNext.isEnabled = false
                buttonPrev.isEnabled = false
                // タイマーの始動
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask(){
                    override fun run() {
                        mHandler.post{
                            if (!mCursor!!.moveToNext()) {
                                mCursor!!.moveToFirst()
                            }
                            setImageInfo(mCursor!!, imageView1)
                        }
                    }
                }, 2000, 2000)
            } else {
                mTimer!!.cancel()
                // タイマー・ボタンの初期化
                mTimer = null
                buttonPauseRestart.text = "再生"
                buttonNext.isEnabled = true
                buttonPrev.isEnabled = true
            }
        }
    }

    private fun setImageInfo(mCursor: Cursor, imageView1: ImageView) {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = mCursor.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView1.setImageURI(imageUri)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCursor!=null) {
            mCursor?.close()
        }
    }

}