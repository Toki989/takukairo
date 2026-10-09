package jp.takukairo.image;
public interface ImageProcessor {
 record Processed(byte[] master,byte[] derivative){}
 Processed process(byte[] bytes);
}
