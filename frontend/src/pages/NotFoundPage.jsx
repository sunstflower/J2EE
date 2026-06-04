import { Link } from "react-router-dom";

function NotFoundPage() {
  return (
    <section className="module-card not-found-card">
      <p className="section-kicker">Not Found</p>
      <h2>页面不存在</h2>
      <p className="lead">当前地址没有对应模块，请返回首页或从导航重新进入。</p>
      <Link className="primary-action text-action" to="/">
        返回首页
      </Link>
    </section>
  );
}

export default NotFoundPage;
