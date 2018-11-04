package zz.app.gif2mp4

import android.util.Log

class FFMpegCommand() {
    val TAG="FFMpegCommand"
    val cmd=ArrayList<String>()
    fun setCmd(vararg args:String) {
        clear()
        args.forEach {
            cmd.add(it)
        }
    }
    fun convertCmd(): Array<String?> {
        printCmd();
        val ret= arrayOfNulls<String>(cmd.size+2)
        ret[0]="ffmpeg"
        ret[1]="-d"
        for(i in 1..cmd.size)
            ret[i+1]= cmd[i-1]
        return ret
    }

    private fun printCmd() {
        var str=""
        cmd.forEach { str+="$it " }
        Log.d(TAG, "cmd = $str")

    }

    fun clear(){
        cmd.clear()
    }
    fun addCmd(command:String){
        cmd.add(command)
    }
}