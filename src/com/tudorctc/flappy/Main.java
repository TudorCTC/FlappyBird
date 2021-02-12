package com.tudorctc.flappy;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import com.tudorctc.flappy.graphics.Shader;
import com.tudorctc.flappy.input.Input;
import com.tudorctc.flappy.level.Level;
import com.tudorctc.flappy.maths.Matrix4f;

public class Main implements Runnable {

	private int width = 1280;
	private int height = 720;

	private Thread thread;
	private boolean running = false;

	private long window;

	private Level level;

	public void start() {
		running = true;
		thread = new Thread(this, "Game");
		thread.start();
	}

	private void init() {
		if (!glfwInit()) {
			// TODO: handle error
			return;
		}

		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		window = glfwCreateWindow(width, height, "Flappy", NULL, NULL);
		if (window == NULL) {
			// TODO handle error
			return;
		}

		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

		glfwSetKeyCallback(window, new Input());

		glfwMakeContextCurrent(window);
		glfwShowWindow(window);
		GL.createCapabilities();

		glEnable(GL_DEPTH_TEST);
		glActiveTexture(GL_TEXTURE1);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		System.out.println("OpenGL: " + glGetString(GL_VERSION));
		Shader.loadAll();

		Shader.BG.enable();
		Matrix4f pr_matrix = Matrix4f.ortographic(-10.0f, 10.0f, -10.0f * 9.0f / 16.0f, 10.0f * 9.0f / 16.0f, -1.0f,
				1.0f);
		Shader.BG.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.BG.setUniform1i("tex", 1);
		Shader.BG.disable();

		Shader.BIRD.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.BIRD.setUniform1i("tex", 1);

		Shader.PIPE.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.PIPE.setUniform1i("tex", 1);

		level = new Level();
	}

	public void run() {
		init();

		long lastTime = System.nanoTime();
		double ns = 1000000000.0 / 60.0;
		double delta = 0.0;
		int updates = 0;
		int frames = 0;
		long timer = System.currentTimeMillis();

		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1.0) {
				update();
				updates++;
				delta--;
			}

			render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println(updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}

			if (glfwWindowShouldClose(window)) {
				running = false;
			}
		}

		glfwDestroyWindow(window);
		glfwTerminate();
	}

	private void update() {
		glfwPollEvents();
		level.update();
		if (level.isGameOver()) {
			level = new Level();
		}
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		level.render();

		int error = glGetError();
		if (error != GL_NO_ERROR) {
			System.out.println(error);
		}

		glfwSwapBuffers(window);
	}

	public static void main(String[] args) {
		new Main().start();

	}

}
