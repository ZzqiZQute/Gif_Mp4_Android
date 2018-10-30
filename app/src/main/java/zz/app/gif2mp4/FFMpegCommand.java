package zz.app.gif2mp4;

import java.util.ArrayList;

class FFMpegCommand {
    public ArrayList<String> getCmds() {
        return cmds;
    }

    public void setCmds(ArrayList<String> cmds) {
        this.cmds = cmds;
    }

    private ArrayList<String> cmds;

    public FFMpegCommand() {

    }

    public FFMpegCommand(ArrayList<String> cmds) {

        this.cmds = cmds;
    }
}
