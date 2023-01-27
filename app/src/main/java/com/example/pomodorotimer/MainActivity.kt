package com.example.pomodorotimer

import android.annotation.SuppressLint
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }

    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }

    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }

    private val soundPool = SoundPool.Builder().build()

    private var currentCountDownTimer: CountDownTimer? = null
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSounds()
    }

    // 앱이 화면에서 보이지 않을 경우
    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    // 다시 앱이 보일 경우
    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    // 더이상 앱을 사용하지 않을 경우
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    // TextView와 SeekBar 조작
    private fun bindViews() {
        // seekBar
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                // seekBar 조작할 때
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if(fromUser){
                        updateRemainTimes(progress * 60 * 1000L)
                    }

                }

                // seekBar 눌렀을 때
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopCountDown()
                }

                // seekBar 조작하다가 뗐을 때
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return   // seekBar가 nullable하면(seekBar가 없으면) countDown 진행 x

                    if(seekBar.progress == 0){
                        stopCountDown() // 0에서 seekBar 떼면 countDown 진행 x
                    }else{
                        startCountDown()    // 다른 부분에서 seekBar 떼면 countDown 진행
                    }

                }
            }

        )
    }

    private fun initSounds() {
        // sound 파일 load 후 메모리에 올리기
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)

    }

    private fun createCountDownTimer(initialMills: Long) =
        object: CountDownTimer(initialMills, 1000L){
            // 1초(countDownInterval)에 한 번씩 호출
            override fun onTick(millisUntilFinished: Long) {
                // 1초마다 UI 갱신(millisUntilFinished : 남은 시간)
                updateRemainTimes(millisUntilFinished)  // TextView 갱신
                updateSeekBar(millisUntilFinished)  // seekBar 갱신
            }

            //
            override fun onFinish() {
                completeCountDown()
            }

        }

    private fun startCountDown(){
        // CountDownTimer 생성 후 시작
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.start()

        // seekBar 시작되는 순간 째깍째깍 소리
        tickingSoundId?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 0, -1, 1f)
        }
    }

    private fun stopCountDown(){
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null

        soundPool.autoPause()
    }

    private fun completeCountDown(){
        // UI 초기화
        updateRemainTimes(0)
        updateSeekBar(0)

        // 끝나는 소리 재생
        soundPool.autoPause()
        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateRemainTimes(remainMillis: Long){
        val remainSeconds = remainMillis / 1000

        // 한자리 수의 숫자도 00~09처럼 나타나게 format 적용
        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)  // 분('까지)
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60)  // 초
    }

    private fun updateSeekBar(remainMillis: Long){
        seekBar.progress = (remainMillis / 1000 / 60).toInt() // 분(Long -> Int)

    }
}