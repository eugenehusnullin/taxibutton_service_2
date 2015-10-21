package tb.dao;

import java.util.List;

import tb.domain.Partner;
import tb.domain.phone.BlackPhone;

public interface IBlackPhoneDao {

	BlackPhone get(Long id);
	
	List<BlackPhone> get(Partner partner);
	
	List<BlackPhone> getAll();
	
	void save(BlackPhone phone);
	
	void saveOrUpdate(BlackPhone phone);
}
