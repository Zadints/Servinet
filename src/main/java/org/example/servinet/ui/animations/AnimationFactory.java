package org.example.servinet.ui.animations;

public class AnimationFactory {

    public LoadingAnimation createLoading() {
        return new LoadingAnimation();
    }

    public SlideUpAnimation createSlideUp() {
        return new SlideUpAnimation();
    }

}
