/**



*/

package mice.servlet;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mice.bean.Order;
import mice.dao.OrderDAO;

public class OrderServlet extends BaseBackServlet {

	public String add(HttpServletRequest request, HttpServletResponse response) {
		return null;
	}

	public String delete(HttpServletRequest request, HttpServletResponse response) {
		return null;
	}

	public String delivery(HttpServletRequest request, HttpServletResponse response) {
		int id = Integer.parseInt(request.getParameter("id"));
		Order o = orderDAO.get(id);
		o.setDeliveryDate(new Date());
		o.setStatus(OrderDAO.waitConfirm);
		orderDAO.update(o);
		return "@admin_order_list";
	}

	public String edit(HttpServletRequest request, HttpServletResponse response) {
		return null;
	}

	public String update(HttpServletRequest request, HttpServletResponse response) {
		return null;
	}

	public String list(HttpServletRequest request, HttpServletResponse response) {
		List<Order> os = orderDAO.list();
		orderItemDAO.fill(os);
		int total = orderDAO.getTotal();

		request.setAttribute("os", os);

		return "admin/listOrder.jsp";
	}
}
