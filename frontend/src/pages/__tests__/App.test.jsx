import { render, screen } from "@testing-library/react";
import App from "../../App";

describe("App", () => {
  it("renders project skeleton message", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "药物管理系统项目骨架已初始化" })).toBeInTheDocument();
  });
});
