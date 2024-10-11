package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.LecternData;

/**
 * {@link LecternData}, but contains {@link ClientBookData} instead of its common variety and supports
 * merging data sent from the server.
 */
public class ClientLecternData extends LecternData<ClientBookData> {
    public ClientLecternData() {
        super(new ClientBookData());
    }

    /**
     * Merge this data with data from the server.
     * @param fromServer Data from the server.
     */
    public void mergeFromServer(LecternData<CommonBookData> fromServer) {
        this.bookData.mergeFromServer(fromServer.bookData);
        this.book = fromServer.book;
    }
}
