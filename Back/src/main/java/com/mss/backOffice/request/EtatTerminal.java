package com.mss.backOffice.request;

public class EtatTerminal {
		private int nbDisconnected;
	    private int nbSupervisor;
	    private int OutOfService;
	    private int OnService;
		public int getNbDisconnected() {
			return nbDisconnected;
		}
		public void setNbDisconnected(int nbDisconnected) {
			this.nbDisconnected = nbDisconnected;
		}
		public int getNbSupervisor() {
			return nbSupervisor;
		}
		public void setNbSupervisor(int nbSupervisor) {
			this.nbSupervisor = nbSupervisor;
		}
		public int getOutOfService() {
			return OutOfService;
		}
		public void setOutOfService(int outOfService) {
			OutOfService = outOfService;
		}
		public int getOnService() {
			return OnService;
		}
		public void setOnService(int onService) {
			OnService = onService;
		}
		public EtatTerminal() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		
}
