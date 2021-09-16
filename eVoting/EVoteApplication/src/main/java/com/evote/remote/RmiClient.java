package com.evote.remote;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * O RmiClient é a interface Admin Console, tem um menu inicial onde permite
 * fazer todas as opereções disponíveis. Pode haver mais que um admin.
 * O cliente localiza o registo do servidor RMI a partir do porto e do nome.
 */
public class RmiClient {
	private RmiServer rmi;

	public RmiServer getRmi() {
		return rmi;
	}
	public void setRmi(RmiServer rmi) {
        this.rmi = rmi;
    }
}