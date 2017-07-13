package jp.live2d.sample;


import java.util.ArrayList;
import java.util.Arrays;

public class Kaomoji {
    public static ArrayList<String> HappyKaomoji = new ArrayList<>(
            Arrays.asList("(*ﾟ∀ﾟ*)", "ξ( ✿＞◡❛)", "(*´∀`)~♥", "(σ′▽‵)′▽‵)σ", "ლ(╹◡╹ლ)", "(ﾉ>ω<)ﾉ"));

    public static ArrayList<String> SurpriseKaomoji = new ArrayList<>(
            Arrays.asList("(((ﾟДﾟ;)))", "(|||ﾟдﾟ)", "Σ(ﾟДﾟ；≡；ﾟдﾟ)", "Σ(;ﾟдﾟ)", "∑(ι´Дン)ノ", "Σ(*ﾟдﾟﾉ)ﾉ"));

    public static ArrayList<String> SadKaomoji = new ArrayList<>(
            Arrays.asList("。･ﾟ･(つд`ﾟ)･ﾟ･", "・゜・(PД`q｡)・゜・", "(´;ω;`)", "இдஇ"));

    public static ArrayList<String> AngryKaomoji = new ArrayList<>(
            Arrays.asList("(#`皿´)", "(╯‵□′)╯︵┴─┴", "(#`Д´)ﾉ"));

    public static ArrayList<String> WonderingKaomoji = new ArrayList<>(
            Arrays.asList("(*´･д･)?", "(｡ŏ_ŏ)", " ( •́ _ •̀)？"));


    public static String randomkaomoji(String input){
        if (input.contains("happy") || input.contains("smile") || input.contains("laugh")){
            return HappyKaomoji.get((int)(Math.random()*HappyKaomoji.size()));
        }
        if (input.contains("surpris") || input.contains("shock")){
            return SurpriseKaomoji.get((int)(Math.random()*SurpriseKaomoji.size()));
        }
        if (input.contains("angry")){
            return AngryKaomoji.get((int)(Math.random()*AngryKaomoji.size()));
        }
        if (input.contains("sad") || input.contains("cry")){
            return SadKaomoji.get((int)(Math.random()*SadKaomoji.size()));
        }
        if (input.contains("?")){
            return WonderingKaomoji.get((int)(Math.random()*WonderingKaomoji.size()));
        }
        return "(´;ω;`) I don't have the kaomoji you're asking.";
    }
}
