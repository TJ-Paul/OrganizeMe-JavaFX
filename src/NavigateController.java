import java.io.IOException;

public interface NavigateController {
	void navigateTo(String fxmlPath, int width, int height) throws IOException;
    void navigateTo(String fxmlPath) throws IOException;
}
