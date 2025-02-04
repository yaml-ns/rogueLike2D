package com.jeu.roguelike2d.utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class SoundPlayer {

    private static boolean loop;
    private static String path;
    private static Clip clip;

    public static Clip play(String soundPath) {
        path = soundPath;
        loop = false;
        return init();
    }

    public static Clip play(String soundPath, boolean loopPlay) {
        path = soundPath;
        loop = loopPlay;
        return init();
    }

    private static Clip init() {
        try {
            URL url = SoundPlayer.class.getResource(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
//            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
//            clip.start();
            return clip;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}
