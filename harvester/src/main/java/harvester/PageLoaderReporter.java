package harvester;

public interface PageLoaderReporter {
    PageLoaderReporter VOID_REPORTER = new PageLoaderReporter() {
        @Override
        public void progress(String message) {
        }
    };
    void progress(String message);
}
