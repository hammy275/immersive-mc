package com.hammy275.immersivemc.common.util;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@JsonAdapter(RGBA.class)
public class RGBA extends TypeAdapter<RGBA> {

    public int r;
    public int g;
    public int b;
    public int a;

    public RGBA(long argb) {
        argb = Mth.clamp(argb, 0, 0xFFFFFFFFL);
        this.a = (int) (argb >> 24);
        this.r = (int) (argb >> 16 & 255);
        this.g = (int) (argb >> 8 & 255);
        this.b = (int) (argb & 255);
    }

    public RGBA(int rgb, int a) {
        this.a = a & 255;
        this.r = rgb >> 16 & 255;
        this.g = rgb >> 8 & 255;
        this.b = rgb & 255;
    }

    public RGBA(int r, int g, int b, int a) {
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static RGBA random() {
        return new RGBA(ThreadLocalRandom.current().nextLong(0xFFFFFFFFL + 1L));
    }

    public long toLong() {
        return (((long) this.a) << 24L) + (((long) this.r) << 16L) + (((long) this.g) << 8L) + this.b;
    }

    public int getRGB() {
        return (this.r << 16L) + (this.g << 8L) + this.b;
    }

    public int getAlpha() {
        return this.a;
    }

    public float redF() {
        return this.r / 255f;
    }

    public float greenF() {
        return this.g / 255f;
    }

    public float blueF() {
        return this.b / 255f;
    }

    public float alphaF() {
        return this.a / 255f;
    }

    public int getColor(char c) {
        return switch (c) {
            case 'r' -> this.r;
            case 'g' -> this.g;
            case 'b' -> this.b;
            case 'a' -> this.a;
            default -> throw new IllegalArgumentException("Only pass 'r', 'g', 'b', or 'a'!");
        };
    }

    public void setColor(char c, int val) {
        switch (c) {
            case 'r' -> this.r = val;
            case 'g' -> this.g = val;
            case 'b' -> this.b = val;
            case 'a' -> this.a = val;
            default -> throw new IllegalArgumentException("Only pass 'r', 'g', 'b', or 'a'!");
        }
    }

    @Override
    public String toString() {
        return "R: %d\tG: %d\tB: %d\ta: %d".formatted(this.r, this.g, this.b, this.a);
    }

    @Override
    public void write(JsonWriter out, RGBA value) throws IOException {
        out.value(value.toLong());
    }

    @Override
    public RGBA read(JsonReader in) throws IOException {
        return new RGBA(in.nextLong());
    }
}
