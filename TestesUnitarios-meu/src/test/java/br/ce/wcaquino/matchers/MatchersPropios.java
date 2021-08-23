package br.ce.wcaquino.matchers;

import java.util.Calendar;

public class MatchersPropios {
	
	public static DiaMesMatcher ehHojeComDiferencaDias(Integer dias) {
		return new DiaMesMatcher(dias);
	}
	
	public static DiaMesMatcher ehHoje() {
		return new DiaMesMatcher(0);
	}
	
	public static DiaSemanaMatcher caiEm(Integer diaSemana) {
		return new DiaSemanaMatcher(diaSemana);
	}

	public static DiaSemanaMatcher caiNaSegunda() {
		return new DiaSemanaMatcher(Calendar.MONDAY);
	}
}
