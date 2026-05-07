from flask import Flask
from routes.categorise import categorise_bp
from routes.query import query_bp
from routes.health import health_bp
from routes.generate_report import generate_report_bp
from routes.describe import describe_bp
from routes.recommend import recommend_bp
from services.sanitizer import sanitize_request
from services.limiter import limiter


app = Flask(__name__)

# Register Middleware
app.before_request(sanitize_request)
limiter.init_app(app)

app.register_blueprint(categorise_bp)
app.register_blueprint(query_bp)
app.register_blueprint(health_bp)
app.register_blueprint(generate_report_bp)
app.register_blueprint(describe_bp)
app.register_blueprint(recommend_bp)


if __name__ == "__main__":
    app.run(debug=True, port=5000)