package br.ce.wcaquino.matchers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DiaMesMatcher extends TypeSafeMatcher<Date> {

	private Integer dias;

	public DiaMesMatcher(Integer dias) {
		this.dias = dias;
	}

	@Override
	public void describeTo(Description description) {
		Date data = DataUtils.obterDataComDiferencaDias(dias);
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		description.appendText(format.format(data));
	}

	@Override
	protected boolean matchesSafely(Date data) {
		return DataUtils.isMesmaData(data, DataUtils.obterDataComDiferencaDias(dias));
	}

}
