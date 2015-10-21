package tb.dao;

import java.util.List;

import tb.domain.Partner;
import tb.domain.Tariff;

public interface ITariffDao {
	List<Tariff> get(Partner partner);

	void saveOrUpdate(Tariff tariff);

	List<Tariff> getAll();
	
	void delete(Partner partner);
}
