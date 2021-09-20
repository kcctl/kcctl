package org.moditect.kcctl.service.compaction;

public interface DatabaseHistoryListener {
    public void started();

    public void stopped();

    public void recoveryStarted();

    public void recoveryStopped();

    /**
     * Invoked for every change read from the history during recovery.
     *
     * @param record
     */
    public void onChangeFromHistory(HistoryRecord record);

    /**
     * Invoked for every change applied and not filtered.
     *
     * @param record
     */
    public void onChangeApplied(HistoryRecord record);

    static DatabaseHistoryListener NOOP = new DatabaseHistoryListener() {
        @Override
        public void stopped() {
        }

        @Override
        public void started() {
        }

        @Override
        public void recoveryStopped() {
        }

        @Override
        public void onChangeFromHistory(HistoryRecord record) {

        }

        @Override
        public void recoveryStarted() {
        }

        @Override
        public void onChangeApplied(HistoryRecord record) {
        }
    };
}

