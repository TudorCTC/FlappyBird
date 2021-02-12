package com.tudorctc.flappy.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Input extends GLFWKeyCallback {

	public static boolean[] keys = new boolean[65536];

	public void invoke(long iwndow, int key, int scancode, int action, int mods) {
		keys[key] = action != GLFW.GLFW_RELEASE;
	}

	public static boolean isKeyDown(int keycode) {
		return keys[keycode];
	}
}
