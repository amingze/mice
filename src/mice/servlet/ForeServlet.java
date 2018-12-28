
package mice.servlet;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.web.util.HtmlUtils;

import mice.bean.Category;
import mice.bean.Order;
import mice.bean.OrderItem;
import mice.bean.Product;
import mice.bean.ProductImage;

import mice.bean.Review;
import mice.bean.User;

import mice.dao.CategoryDAO;
import mice.dao.OrderDAO;
import mice.dao.ProductDAO;
import mice.dao.ProductImageDAO;

public class ForeServlet extends BaseForeServlet {
	public String home(HttpServletRequest request, HttpServletResponse response) {
		List<Category> cs = new CategoryDAO().list();
		new ProductDAO().fill(cs);
		request.setAttribute("cs", cs);
		return "home.jsp";
	}

	public String login(HttpServletRequest request, HttpServletResponse response) {
		String name = request.getParameter("name");
		name = HtmlUtils.htmlEscape(name);
		String password = request.getParameter("password");

		User user = userDAO.get(name, password);

		if (null == user || "" == password) {
			request.setAttribute("msg", "账号密码错误");
			return "login.jsp";
		}
		request.getSession().setAttribute("user", user);
		return "@forehome";
	}

	public String logout(HttpServletRequest request, HttpServletResponse response) {
		request.getSession().removeAttribute("user");
		return "@forehome";
	}

	public String product(HttpServletRequest request, HttpServletResponse response) {
		int pid = Integer.parseInt(request.getParameter("pid"));
		Product p = productDAO.get(pid);

		String edit = productDAO.get(pid).getEdit();
		List<ProductImage> productSingleImages = productImageDAO.list(p);

		p.setProductSingleImages(productSingleImages);

		List<Review> reviews = reviewDAO.list(p.getId());
		System.out.println("!edit!:" + edit);
		productDAO.setSaleAndReviewNumber(p);

		request.setAttribute("reviews", reviews);

		request.setAttribute("p", p);

		return "product.jsp";
	}

	public String checkLogin(HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute("user");
		if (null != user)
			return "%success";
		return "%fail";
	}

	public String loginAjax(HttpServletRequest request, HttpServletResponse response) {
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		User user = userDAO.get(name, password);

		if (null == user) {
			return "%fail";
		}
		request.getSession().setAttribute("user", user);
		return "%success";
	}

	public String category(HttpServletRequest request, HttpServletResponse response) {
		int cid = Integer.parseInt(request.getParameter("cid"));
		Category c = new CategoryDAO().get(cid);
		new ProductDAO().fill(c);
		new ProductDAO().setSaleAndReviewNumber(c.getProducts());
		request.setAttribute("c", c);
		return "category.jsp";
	}

	public String search(HttpServletRequest request, HttpServletResponse response) {
		String keyword = request.getParameter("keyword");

		List<Product> ps = new ProductDAO().search(keyword, 0, 20);

		productDAO.setSaleAndReviewNumber(ps);
		//
		request.setAttribute("ps", ps);
		return "searchResult.jsp";
	}

	public String buyone(HttpServletRequest request, HttpServletResponse response) {
		int pid = Integer.parseInt(request.getParameter("pid"));
		int num = Integer.parseInt(request.getParameter("num"));
		Product p = productDAO.get(pid);
		int oiid = 0;

		User user = (User) request.getSession().getAttribute("user");
		boolean found = false;
		List<OrderItem> ois = orderItemDAO.listByUser(user.getId());

		OrderItem oi = new OrderItem();
		oi.setUser(user);
		oi.setNumber(num);
		oi.setProduct(p);
		orderItemDAO.add(oi);
		oiid = oi.getId();

		return "@forebuy?oiid=" + oiid;
	}

	public String buy(HttpServletRequest request, HttpServletResponse response) {
		String[] oiids = request.getParameterValues("oiid");
		List<OrderItem> ois = new ArrayList<>();
		float total = 0;

		for (String strid : oiids) {
			int oiid = Integer.parseInt(strid);
			OrderItem oi = orderItemDAO.get(oiid);
			total += oi.getProduct().getPromotePrice() * oi.getNumber();
			ois.add(oi);
		}

		request.getSession().setAttribute("ois", ois);
		request.setAttribute("total", total);
		return "buy.jsp";
	}

	public String addCart(HttpServletRequest request, HttpServletResponse response) {
		int pid = Integer.parseInt(request.getParameter("pid"));
		Product p = productDAO.get(pid);
		int num = Integer.parseInt(request.getParameter("num"));

		User user = (User) request.getSession().getAttribute("user");
		boolean found = false;

		List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
		for (OrderItem oi : ois) {
			if (oi.getProduct().getId() == p.getId()) {
				oi.setNumber(oi.getNumber() + num);
				orderItemDAO.update(oi);
				found = true;
				break;
			}
		}

		if (!found) {
			OrderItem oi = new OrderItem();
			oi.setUser(user);
			oi.setNumber(num);
			oi.setProduct(p);
			orderItemDAO.add(oi);
		}
		return "%success";
	}

	public String cart(HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute("user");
		List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
		request.setAttribute("ois", ois);
		return "cart.jsp";
	}

	public String changeOrderItem(HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute("user");
		if (null == user)
			return "%fail";

		int pid = Integer.parseInt(request.getParameter("pid"));
		int number = Integer.parseInt(request.getParameter("number"));
		List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
		for (OrderItem oi : ois) {
			if (oi.getProduct().getId() == pid) {
				oi.setNumber(number);
				orderItemDAO.update(oi);
				break;
			}

		}
		return "%success";
	}

	public String deleteOrderItem(HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute("user");
		if (null == user)
			return "%fail";
		int oiid = Integer.parseInt(request.getParameter("oiid"));
		orderItemDAO.delete(oiid);
		return "%success";
	}

	public String createOrder(HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute("user");

		String address = request.getParameter("address");
		String post = request.getParameter("post");
		String receiver = request.getParameter("receiver");
		String mobile = request.getParameter("mobile");
		String userMessage = request.getParameter("userMessage");

		String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
		Order order = new Order();
		order.setOrderCode(orderCode);
		order.setAddress(address);
		order.setPost(post);
		order.setReceiver(receiver);
		order.setMobile(mobile);
		order.setUserMessage(userMessage);
		order.setCreateDate(new Date());
		order.setUser(user);
		order.setStatus(OrderDAO.waitPay);

		orderDAO.add(order);

		List<OrderItem> ois = (List<OrderItem>) request.getSession().getAttribute("ois");
		float total = 0;
		for (OrderItem oi : ois) {
			oi.setOrder(order);
			orderItemDAO.update(oi);
			total += oi.getProduct().getPromotePrice() * oi.getNumber();
		}

		return "@forealipay?oid=" + order.getId() + "&total=" + total;
	}

	public String alipay(HttpServletRequest request, HttpServletResponse response) {
		return "alipay.jsp";
	}

	public String payed(HttpServletRequest request, HttpServletResponse response) {
		int oid = Integer.parseInt(request.getParameter("oid"));
		Order order = orderDAO.get(oid);
		order.setStatus(OrderDAO.waitDelivery);
		order.setPayDate(new Date());
		new OrderDAO().update(order);
		request.setAttribute("o", order);
		return "payed.jsp";
	}

	public String bought(HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute("user");
		List<Order> os = orderDAO.list(user.getId(), OrderDAO.delete);

		orderItemDAO.fill(os);

		request.setAttribute("os", os);

		return "bought.jsp";
	}

	public String confirmPay(HttpServletRequest request, HttpServletResponse response) {
		int oid = Integer.parseInt(request.getParameter("oid"));
		Order o = orderDAO.get(oid);
		orderItemDAO.fill(o);
		request.setAttribute("o", o);
		return "confirmPay.jsp";
	}

	public String orderConfirmed(HttpServletRequest request, HttpServletResponse response) {
		int oid = Integer.parseInt(request.getParameter("oid"));
		Order o = orderDAO.get(oid);
		o.setStatus(OrderDAO.waitReview);
		o.setConfirmDate(new Date());
		orderDAO.update(o);
		return "orderConfirmed.jsp";
	}

	public String deleteOrder(HttpServletRequest request, HttpServletResponse response) {
		int oid = Integer.parseInt(request.getParameter("oid"));
		Order o = orderDAO.get(oid);
		o.setStatus(OrderDAO.delete);
		orderDAO.update(o);
		return "%success";
	}

	public String review(HttpServletRequest request, HttpServletResponse response) {
		int oid = Integer.parseInt(request.getParameter("oid"));
		Order o = orderDAO.get(oid);
		orderItemDAO.fill(o);
		Product p = o.getOrderItems().get(0).getProduct();
		List<Review> reviews = reviewDAO.list(p.getId());
		productDAO.setSaleAndReviewNumber(p);
		request.setAttribute("p", p);
		request.setAttribute("o", o);
		request.setAttribute("reviews", reviews);
		return "review.jsp";
	}

	public String doreview(HttpServletRequest request, HttpServletResponse response) {
		int oid = Integer.parseInt(request.getParameter("oid"));
		Order o = orderDAO.get(oid);
		o.setStatus(OrderDAO.finish);
		orderDAO.update(o);
		int pid = Integer.parseInt(request.getParameter("pid"));
		Product p = productDAO.get(pid);

		String content = request.getParameter("content");

		content = HtmlUtils.htmlEscape(content);

		User user = (User) request.getSession().getAttribute("user");
		Review review = new Review();
		review.setContent(content);
		review.setProduct(p);
		review.setCreateDate(new Date());
		review.setUser(user);
		reviewDAO.add(review);

		return "@forereview?oid=" + oid + "&fine=true";
	}

	public String registerSuccess(HttpServletRequest request, HttpServletResponse response) {

		return "@registerSuccess.jsp";
	}

	public String register(HttpServletRequest request, HttpServletResponse response) {
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		name = HtmlUtils.htmlEscape(name);
		System.out.println(name);
		boolean exist = userDAO.isExist(name);
		boolean erro = ("" == name || "" == password);
		boolean nodate = (null == name);

		if (exist) {

			request.setAttribute("msg", "ERRO");

			return "register.jsp";
		} else if (erro) {
			request.setAttribute("msg", "PLZ INPUT");
			return "register.jsp";
		} else if (nodate) {
			return "register.jsp";
		}

		User user = new User();
		user.setName(name);
		user.setPassword(password);
		System.out.println(user.getName());
		System.out.println(user.getPassword());
		userDAO.add(user);

		return "@foreregisterSuccess";
	}

}
